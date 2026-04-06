package com.pindou.timer.service.impl;

import cn.hutool.json.JSONObject;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.constants.TableConstants;
import com.pindou.timer.dto.AmountDetail;
import com.pindou.timer.dto.DurationCalculation;
import com.pindou.timer.dto.EndTableRequest;
import com.pindou.timer.dto.OrderAmountCalculation;
import com.pindou.timer.entity.ConsumptionRecord;
import com.pindou.timer.entity.Member;
import com.pindou.timer.entity.Order;
import com.pindou.timer.entity.Table;
import com.pindou.timer.mapper.ConsumptionRecordMapper;
import com.pindou.timer.mapper.MemberMapper;
import com.pindou.timer.service.BillingService;
import com.pindou.timer.service.MemberDiscountService;
import com.pindou.timer.service.OrderSettlementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 订单结算服务实现类
 *
 * @author wuci
 * @date 2026-04-06
 */
@Slf4j
@Service
public class OrderSettlementServiceImpl implements OrderSettlementService {

    @Autowired
    private BillingService billingService;

    @Autowired
    private MemberDiscountService memberDiscountService;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private ConsumptionRecordMapper consumptionRecordMapper;

    @Override
    public DurationCalculation calculateDuration(Table table, long now) {
        long start = table.getStartTime();
        int totalDuration = (int) ((now - start) / 1000);

        int pauseDuration = table.getPauseAccumulated();
        if (TableConstants.Status.PAUSED.equals(table.getStatus()) && table.getLastPauseTime() != null) {
            pauseDuration += (int) ((now - table.getLastPauseTime()) / 1000);
        }

        return DurationCalculation.of(totalDuration, pauseDuration);
    }

    @Override
    public String determineOrderStatus(int actualDuration, int invalidOrderThresholdSeconds) {
        if (invalidOrderThresholdSeconds > 0 && actualDuration < invalidOrderThresholdSeconds) {
            log.info("订单使用时长不足{}分钟，将标记为作废: actualDuration={}秒",
                    invalidOrderThresholdSeconds / 60, actualDuration);
            return TableConstants.OrderStatus.CANCELLED;
        }
        return TableConstants.OrderStatus.COMPLETED;
    }

    @Override
    public OrderAmountCalculation calculateOrderAmount(Order order, Table table, DurationCalculation durationCalc, Long memberId) {
        // 计算费用
        if (order.getAmount() != null && order.getAmount().doubleValue() > 0) {
            // 使用订单中累加的费用
            double finalAmount = order.getAmount().doubleValue();
            double originalAmount = order.getOriginalAmount() != null && order.getOriginalAmount().doubleValue() > 0
                    ? order.getOriginalAmount().doubleValue()
                    : finalAmount;

            // 构建费用明细结构
            AmountDetail amountDetail = AmountDetail.builder()
                    .totalAmount(finalAmount)
                    .normalAmount(finalAmount)
                    .overtimeAmount(0.0)
                    .billingType("preset")
                    .actualDuration(durationCalc.getActualDuration())
                    .unitPrice(finalAmount)
                    .overtimeRate(1.0)
                    .build();

            log.info("使用订单累加费用: orderId={}, originalAmount={}, finalAmount={}",
                    order.getId(), originalAmount, finalAmount);

            return OrderAmountCalculation.of(originalAmount, finalAmount, amountDetail);
        }

        // 重新计算费用
        String orderChannel = order.getChannel() != null ? order.getChannel() : TableConstants.Channel.STORE;
        AmountDetail amountDetail = billingService.calculateAmount(orderChannel, durationCalc.getActualDuration(), table.getPresetDuration());
        double originalAmount = amountDetail.getTotalAmount();
        double finalAmount = originalAmount;

        log.info("使用渠道 {} 重新计算费用: orderId={}, originalAmount={}", orderChannel, order.getId(), originalAmount);

        return OrderAmountCalculation.of(originalAmount, finalAmount, amountDetail);
    }

    @Override
    public String buildAmountDetailJson(AmountDetail amountDetail, int actualDuration) {
        JSONObject amountJson = new JSONObject();
        amountJson.set("normalAmount", amountDetail.getNormalAmount());
        amountJson.set("overtimeAmount", amountDetail.getOvertimeAmount());
        amountJson.set("totalAmount", amountDetail.getTotalAmount());
        amountJson.set("actualDuration", actualDuration);
        amountJson.set("billingType", amountDetail.getBillingType());
        amountJson.set("unitPrice", amountDetail.getUnitPrice());
        amountJson.set("overtimeRate", amountDetail.getOvertimeRate());
        return amountJson.toString();
    }

    @Override
    public void updateOrderForEndTimer(Order order, DurationCalculation durationCalc, OrderAmountCalculation amountCalc,
                                      String amountDetailJson, String orderStatus, EndTableRequest request, long now, Long memberId) {
        order.setEndTime(now);
        order.setDuration(durationCalc.getTotalDuration());
        order.setPauseDuration(durationCalc.getPauseDuration());
        order.setOriginalAmount(BigDecimal.valueOf(amountCalc.getOriginalAmount()));
        order.setAmount(BigDecimal.valueOf(amountCalc.getFinalAmount()));
        order.setMemberId(memberId);
        order.setAmountDetail(amountDetailJson);
        order.setStatus(orderStatus);
        order.setPaidAt(TableConstants.isOrderCompleted(orderStatus) ? now : null);
        order.setUpdatedAt(now);

        // 更新支付方式
        if (request != null && request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod());
            log.info("更新订单支付方式: orderId={}, paymentMethod={}", order.getId(), request.getPaymentMethod());
        }
    }

    @Override
    public void processBalanceDeduction(Order order, double finalAmount, String orderStatus) {
        // 只处理已完成且有支付方式的订单
        if (order.getMemberId() == null || order.getPaymentMethod() == null || !TableConstants.isOrderCompleted(orderStatus)) {
            return;
        }

        String paymentMethod = order.getPaymentMethod();

        // 只处理余额支付和组合支付
        if (!TableConstants.isBalancePayment(paymentMethod)) {
            return;
        }

        // 查询会员信息
        Member member = memberMapper.selectById(order.getMemberId());
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员信息不存在");
        }

        BigDecimal balance = member.getBalance();
        BigDecimal finalAmountBigDecimal = BigDecimal.valueOf(finalAmount);
        BigDecimal balanceAmount = BigDecimal.ZERO;
        BigDecimal otherAmount = BigDecimal.ZERO;

        // 计算支付拆分
        if (TableConstants.PaymentMethod.BALANCE.equals(paymentMethod)) {
            // 纯余额支付：验证余额是否充足
            if (balance.compareTo(finalAmountBigDecimal) < 0) {
                throw new BusinessException(ErrorCode.INVALID_PARAM,
                        "余额不足，当前余额：" + balance + "元，需支付：" + finalAmountBigDecimal + "元");
            }
            balanceAmount = finalAmountBigDecimal;
        } else {
            // 组合支付：优先使用余额
            if (balance.compareTo(finalAmountBigDecimal) >= 0) {
                balanceAmount = finalAmountBigDecimal;
            } else {
                balanceAmount = balance;
                otherAmount = finalAmountBigDecimal.subtract(balance);
            }
        }

        // 扣除余额
        BigDecimal balanceBefore = member.getBalance();
        member.setBalance(member.getBalance().subtract(balanceAmount));
        member.setTotalAmount(member.getTotalAmount().add(finalAmountBigDecimal));
        memberMapper.updateById(member);

        // 插入消费记录
        ConsumptionRecord consumptionRecord = new ConsumptionRecord();
        consumptionRecord.setMemberId(member.getId());
        consumptionRecord.setMemberName(member.getName());
        consumptionRecord.setMemberPhone(member.getPhone());
        consumptionRecord.setOrderId(order.getId());
        consumptionRecord.setAmount(balanceAmount);
        consumptionRecord.setBalanceBefore(balanceBefore);
        consumptionRecord.setBalanceAfter(member.getBalance());
        consumptionRecord.setRemark("桌台消费-订单号:" + order.getId());
        consumptionRecord.setCreatedAt(System.currentTimeMillis());
        consumptionRecordMapper.insert(consumptionRecord);

        // 更新订单支付信息
        order.setBalanceAmount(balanceAmount);
        order.setOtherPaymentAmount(otherAmount);

        log.info("余额扣除成功 - 会员ID:{}, 支付方式:{}, 扣除金额:{}, 余额剩余:{}, 消费记录ID:{}",
                member.getId(), paymentMethod, balanceAmount, member.getBalance(), consumptionRecord.getId());
    }
}

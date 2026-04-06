package com.pindou.timer.service;

import com.pindou.timer.dto.AmountDetail;
import com.pindou.timer.dto.DurationCalculation;
import com.pindou.timer.dto.EndTableRequest;
import com.pindou.timer.dto.OrderAmountCalculation;
import com.pindou.timer.entity.Order;
import com.pindou.timer.entity.Table;

/**
 * 订单结算服务接口
 *
 * @author wuci
 * @date 2026-04-06
 */
public interface OrderSettlementService {

    /**
     * 计算时长
     *
     * @param table 桌台信息
     * @param now 当前时间戳
     * @return 时长计算结果
     */
    DurationCalculation calculateDuration(Table table, long now);

    /**
     * 判断订单状态
     *
     * @param actualDuration 实际使用时长（秒）
     * @param invalidOrderThresholdSeconds 无效订单时间阈值（秒）
     * @return 订单状态：completed-已完成, cancelled-已作废
     */
    String determineOrderStatus(int actualDuration, int invalidOrderThresholdSeconds);

    /**
     * 计算订单费用
     *
     * @param order 订单信息
     * @param table 桌台信息
     * @param durationCalc 时长计算结果
     * @param memberId 会员ID
     * @return 费用计算结果
     */
    OrderAmountCalculation calculateOrderAmount(Order order, Table table, DurationCalculation durationCalc, Long memberId);

    /**
     * 构建金额明细JSON
     *
     * @param amountDetail 费用明细
     * @param actualDuration 实际时长
     * @return JSON字符串
     */
    String buildAmountDetailJson(AmountDetail amountDetail, int actualDuration);

    /**
     * 更新订单（结账）
     *
     * @param order 订单信息
     * @param durationCalc 时长计算结果
     * @param amountCalc 费用计算结果
     * @param amountDetailJson 金额明细JSON
     * @param orderStatus 订单状态
     * @param request 结账请求
     * @param now 当前时间戳
     * @param memberId 会员ID
     */
    void updateOrderForEndTimer(Order order, DurationCalculation durationCalc, OrderAmountCalculation amountCalc,
                                String amountDetailJson, String orderStatus, EndTableRequest request, long now, Long memberId);

    /**
     * 处理余额扣除
     *
     * @param order 订单信息
     * @param finalAmount 最终金额
     * @param orderStatus 订单状态
     */
    void processBalanceDeduction(Order order, double finalAmount, String orderStatus);
}

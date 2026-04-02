package com.pindou.timer.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Member;
import com.pindou.timer.entity.MemberLevel;
import com.pindou.timer.entity.Order;
import com.pindou.timer.mapper.OrderMapper;
import com.pindou.timer.service.BillingService;
import com.pindou.timer.service.MemberLevelService;
import com.pindou.timer.service.MemberService;
import com.pindou.timer.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单Service实现类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private BillingService billingService;

    @Resource
    private MemberService memberService;

    @Resource
    private MemberLevelService memberLevelService;

    /**
     * 获取今天的起始时间（0点0分0秒）
     */
    private long getTodayStartTime() {
        return System.currentTimeMillis() / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000) - 8 * 60 * 60 * 1000;
    }

    @Override
    public PageResult<OrderInfoResponse> getActiveOrders(Integer page, Integer pageSize) {
        log.info("获取当天已完成订单列表: page={}, pageSize={}", page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        // 查询当天已完成的订单
        wrapper.eq(Order::getStatus, "completed");
        // 查询创建时间在今天的订单
        wrapper.ge(Order::getCreatedAt, getTodayStartTime());

        // 按创建时间倒序
        wrapper.orderByDesc(Order::getCreatedAt);

        // 分页查询
        Page<Order> pageObj = new Page<>(page, pageSize);
        Page<Order> resultPage = orderMapper.selectPage(pageObj, wrapper);

        // 转换结果
        List<OrderInfoResponse> records = resultPage.getRecords().stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());

        // 使用 PageResult.of 构建分页结果
        PageResult<OrderInfoResponse> pageResult = PageResult.of(
                records,
                resultPage.getTotal(),
                page,
                pageSize
        );

        log.info("获取当天已完成订单列表成功: total={}, totalPages={}", pageResult.getTotal(), pageResult.getTotalPages());
        return pageResult;
    }

    @Override
    public PageResult<OrderInfoResponse> getHistoryOrders(OrderQueryRequest request) {
        log.info("获取历史订单列表: request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 状态筛选：如果未指定状态或选择"all"，查询所有状态的订单
        if (StringUtils.isNotBlank(request.getStatus()) && !"all".equals(request.getStatus())) {
            wrapper.eq(Order::getStatus, request.getStatus());
        }
        // status 为空或 "all" 时，不过滤状态，查询所有订单

        // 桌台筛选
        if (request.getTableId() != null) {
            wrapper.eq(Order::getTableId, request.getTableId());
        }

        // 时间范围筛选
        if (request.getStartTime() != null) {
            wrapper.ge(Order::getStartTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(Order::getStartTime, request.getEndTime());
        }

        // 关键词搜索（桌台名称/操作员）
        if (StringUtils.isNotBlank(request.getKeyword())) {
            String keyword = request.getKeyword();
            wrapper.and(w -> w.like(Order::getTableName, keyword)
                    .or()
                    .like(Order::getOperatorName, keyword));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Order::getCreatedAt);

        // 分页查询
        Page<Order> page = new Page<>(request.getPage(), request.getPageSize());
        Page<Order> resultPage = orderMapper.selectPage(page, wrapper);

        // 转换结果
        List<OrderInfoResponse> records = resultPage.getRecords().stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());

        // 使用 PageResult.of 构建分页结果
        PageResult<OrderInfoResponse> pageResult = PageResult.of(
                records,
                resultPage.getTotal(),
                request.getPage(),
                request.getPageSize()
        );

        log.info("获取历史订单列表成功: total={}, totalPages={}", pageResult.getTotal(), pageResult.getTotalPages());
        return pageResult;
    }

    @Override
    public OrderDetailResponse getOrderDetail(String orderId) {
        log.info("获取订单详情: orderId={}", orderId);

        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 转换为详情响应
        OrderDetailResponse response = convertToDetailResponse(order);

        log.info("获取订单详情成功: orderId={}", orderId);
        return response;
    }

    @Override
    public List<OrderInfoResponse> exportOrders(OrderQueryRequest request) {
        log.info("导出订单: request={}", request);

        // 设置较大的分页用于导出
        request.setPage(1);
        request.setPageSize(50000);

        PageResult<OrderInfoResponse> pageResult = getHistoryOrders(request);

        log.info("导出订单成功: count={}", pageResult.getList().size());
        return pageResult.getList();
    }

    /**
     * 转换为订单信息响应（列表用）
     */
    private OrderInfoResponse convertToInfoResponse(Order order) {
        OrderInfoResponse response = new OrderInfoResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getId()); // 订单编号使用ID
        response.setTableId(order.getTableId());
        response.setTableName(order.getTableName());
        response.setStartTime(order.getStartTime());
        response.setEndTime(order.getEndTime());
        response.setDuration(order.getDuration());
        response.setPauseDuration(order.getPauseDuration());
        response.setPresetDuration(order.getPresetDuration());
        response.setChannel(order.getChannel());
        response.setStatus(order.getStatus());
        response.setOperatorName(order.getOperatorName());
        response.setPaidAt(order.getPaidAt());
        response.setCreatedAt(order.getCreatedAt());

        // 调试日志：打印订单的所有字段
        log.info("转换订单信息: orderId={}, memberId={}, originalAmount={}, amount={}",
                order.getId(), order.getMemberId(), order.getOriginalAmount(), order.getAmount());

        // 设置会员ID
        if (order.getMemberId() != null) {
            response.setMemberId(order.getMemberId());
        }

        // 优先使用订单中存储的金额，如果没有则重新计算
        if (order.getAmount() != null && order.getAmount().doubleValue() > 0) {
            response.setAmount(order.getAmount().doubleValue());

            // 设置原价
            if (order.getOriginalAmount() != null && order.getOriginalAmount().doubleValue() > 0) {
                response.setOriginalAmount(order.getOriginalAmount().doubleValue());
                // 计算折扣金额
                double discountAmount = order.getOriginalAmount().doubleValue() - order.getAmount().doubleValue();
                if (discountAmount > 0) {
                    response.setDiscountAmount(discountAmount);
                }
            }

            log.info("使用订单存储金额: orderId={}, amount={}", order.getId(), order.getAmount().doubleValue());
        } else {
            // 计算当前费用，使用订单中存储的渠道
            int actualDuration = order.getDuration() - order.getPauseDuration();
            String orderChannel = order.getChannel() != null ? order.getChannel() : "store";
            AmountDetail amountDetail = billingService.calculateAmount(orderChannel, actualDuration, order.getPresetDuration());
            response.setAmount(amountDetail.getTotalAmount());
            log.info("重新计算订单金额: orderId={}, channel={}, amount={}", order.getId(), orderChannel, amountDetail.getTotalAmount());
        }

        // 填充会员信息
        if (order.getMemberId() != null) {
            try {
                log.info("查询会员信息: orderId={}, memberId={}", order.getId(), order.getMemberId());
                Member member = memberService.getById(order.getMemberId());
                if (member != null) {
                    response.setMemberName(member.getName());
                    log.info("查询到会员: orderId={}, memberId={}, memberName={}", order.getId(), order.getMemberId(), member.getName());

                    // 获取会员等级信息
                    if (member.getLevelId() != null) {
                        MemberLevel memberLevel = memberLevelService.getById(member.getLevelId());
                        if (memberLevel != null) {
                            response.setMemberLevelName(memberLevel.getName());
                            if (memberLevel.getDiscountRate() != null) {
                                response.setMemberDiscountRate(memberLevel.getDiscountRate().doubleValue());
                            }
                            log.info("查询到会员等级: orderId={}, levelName={}, discountRate={}", order.getId(), memberLevel.getName(), memberLevel.getDiscountRate());
                        }
                    }
                } else {
                    log.warn("会员不存在: orderId={}, memberId={}", order.getId(), order.getMemberId());
                }
            } catch (Exception e) {
                log.error("获取会员信息异常: orderId={}, memberId={}, error={}", order.getId(), order.getMemberId(), e.getMessage(), e);
            }
        }

        return response;
    }

    /**
     * 转换为订单详情响应
     */
    private OrderDetailResponse convertToDetailResponse(Order order) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(order.getId());
        response.setTableId(order.getTableId());
        response.setTableName(order.getTableName());
        response.setChannel(order.getChannel());
        response.setStartTime(order.getStartTime());
        response.setEndTime(order.getEndTime());
        response.setPaidAt(order.getPaidAt());
        response.setDuration(order.getDuration());
        response.setPauseDuration(order.getPauseDuration());
        response.setPresetDuration(order.getPresetDuration());
        response.setStatus(order.getStatus());
        response.setOperatorId(order.getOperatorId());
        response.setOperatorName(order.getOperatorName());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // 计算计费时长
        int actualDuration = order.getDuration() - order.getPauseDuration();
        response.setActualDuration(actualDuration);

        // 优先使用订单中存储的金额和金额明细，如果没有则重新计算
        if (order.getAmount() != null && order.getAmount().doubleValue() > 0) {
            response.setAmount(order.getAmount().doubleValue());

            // 设置原价
            if (order.getOriginalAmount() != null && order.getOriginalAmount().doubleValue() > 0) {
                response.setOriginalAmount(order.getOriginalAmount().doubleValue());
            }

            // 尝试解析金额明细JSON
            if (StringUtils.isNotBlank(order.getAmountDetail())) {
                try {
                    JSONObject jsonObject = JSONUtil.parseObj(order.getAmountDetail());
                    response.setNormalAmount(jsonObject.getDouble("normalAmount"));
                    response.setOvertimeAmount(jsonObject.getDouble("overtimeAmount"));

                    // 构建AmountDetail对象
                    AmountDetail amountDetail = new AmountDetail();
                    amountDetail.setTotalAmount(order.getAmount().doubleValue());
                    amountDetail.setNormalAmount(response.getNormalAmount());
                    amountDetail.setOvertimeAmount(response.getOvertimeAmount());
                    amountDetail.setActualDuration(actualDuration);
                    amountDetail.setBillingType(jsonObject.getStr("billingType"));
                    amountDetail.setUnitPrice(jsonObject.getDouble("unitPrice"));
                    amountDetail.setOvertimeRate(jsonObject.getDouble("overtimeRate"));
                    response.setAmountDetailInfo(amountDetail);

                    log.info("使用订单存储的金额明细: orderId={}, amount={}", order.getId(), order.getAmount().doubleValue());
                } catch (Exception e) {
                    log.warn("解析金额明细失败，重新计算: orderId={}, amountDetail={}", order.getId(), order.getAmountDetail());
                    // 解析失败，重新计算，使用订单中存储的渠道
                    String orderChannel = order.getChannel() != null ? order.getChannel() : "store";
                    AmountDetail amountDetail = billingService.calculateAmount(orderChannel, actualDuration, order.getPresetDuration());
                    response.setNormalAmount(amountDetail.getNormalAmount());
                    response.setOvertimeAmount(amountDetail.getOvertimeAmount());
                    response.setAmountDetailInfo(amountDetail);
                    log.info("使用渠道 {} 重新计算订单金额明细: orderId={}, amount={}", orderChannel, order.getId(), amountDetail.getTotalAmount());
                }
            } else {
                // 没有金额明细，使用订单金额创建简单的明细
                AmountDetail amountDetail = new AmountDetail();
                amountDetail.setTotalAmount(order.getAmount().doubleValue());
                amountDetail.setNormalAmount(order.getAmount().doubleValue());
                amountDetail.setOvertimeAmount(0.0);
                amountDetail.setActualDuration(actualDuration);
                amountDetail.setBillingType("preset");
                amountDetail.setUnitPrice(order.getAmount().doubleValue());
                amountDetail.setOvertimeRate(1.0);
                response.setNormalAmount(order.getAmount().doubleValue());
                response.setOvertimeAmount(0.0);
                response.setAmountDetailInfo(amountDetail);
            }
        } else {
            // 订单没有金额，重新计算，使用订单中存储的渠道
            String orderChannel = order.getChannel() != null ? order.getChannel() : "store";
            AmountDetail amountDetail = billingService.calculateAmount(orderChannel, actualDuration, order.getPresetDuration());
            response.setNormalAmount(amountDetail.getNormalAmount());
            response.setOvertimeAmount(amountDetail.getOvertimeAmount());
            response.setAmount(amountDetail.getTotalAmount());
            log.info("使用渠道 {} 重新计算订单金额: orderId={}, amount={}", orderChannel, order.getId(), amountDetail.getTotalAmount());
            response.setAmountDetailInfo(amountDetail);
            log.info("重新计算订单金额: orderId={}, amount={}", order.getId(), amountDetail.getTotalAmount());
        }

        // 填充会员信息
        if (order.getMemberId() != null) {
            try {
                Member member = memberService.getById(order.getMemberId());
                if (member != null) {
                    response.setMemberId(member.getId());
                    response.setMemberName(member.getName());

                    // 获取会员等级信息
                    if (member.getLevelId() != null) {
                        MemberLevel memberLevel = memberLevelService.getById(member.getLevelId());
                        if (memberLevel != null) {
                            response.setMemberLevelName(memberLevel.getName());
                            if (memberLevel.getDiscountRate() != null) {
                                response.setMemberDiscountRate(memberLevel.getDiscountRate().doubleValue());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取订单详情会员信息失败: orderId={}, memberId={}, error={}", order.getId(), order.getMemberId(), e.getMessage());
            }
        }

        // 构建时间线
        List<TimeLineItem> timeLine = buildTimeLine(order);
        response.setTimeLine(timeLine);

        return response;
    }

    /**
     * 构建时间线记录
     * 注：实际项目中应从操作日志表读取，这里先简化处理
     */
    private List<TimeLineItem> buildTimeLine(Order order) {
        List<TimeLineItem> timeLine = new ArrayList<>();

        // 开始记录
        TimeLineItem startItem = new TimeLineItem();
        startItem.setTime(order.getStartTime());
        startItem.setAction("start");
        startItem.setDescription("开始计时");
        startItem.setOperator(order.getOperatorName());
        timeLine.add(startItem);

        // 结束记录
        if (order.getEndTime() != null) {
            TimeLineItem endItem = new TimeLineItem();
            endItem.setTime(order.getEndTime());
            endItem.setAction("end");
            endItem.setDescription("结束计时并结账");
            endItem.setOperator(order.getOperatorName());
            timeLine.add(endItem);
        }

        // 支付记录
        if (order.getPaidAt() != null) {
            TimeLineItem paidItem = new TimeLineItem();
            paidItem.setTime(order.getPaidAt());
            paidItem.setAction("paid");
            paidItem.setDescription("支付完成");
            paidItem.setOperator(order.getOperatorName());
            timeLine.add(paidItem);
        }

        return timeLine;
    }
}

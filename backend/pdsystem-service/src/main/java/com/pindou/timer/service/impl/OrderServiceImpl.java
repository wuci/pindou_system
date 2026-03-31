package com.pindou.timer.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Order;
import com.pindou.timer.mapper.OrderMapper;
import com.pindou.timer.service.BillingService;
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

    /**
     * 获取今天的起始时间（0点0分0秒）
     */
    private long getTodayStartTime() {
        return System.currentTimeMillis() / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000) - 8 * 60 * 60 * 1000;
    }

    @Override
    public List<OrderInfoResponse> getActiveOrders() {
        log.info("获取当前订单列表");

        // 获取今天0点的时间戳
        long todayStartTime = getTodayStartTime();

        // 查询今天已完成的订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, "completed")
                .ge(Order::getStartTime, todayStartTime)
                .orderByDesc(Order::getStartTime);

        List<Order> orders = orderMapper.selectList(wrapper);

        // 转换为响应DTO
        List<OrderInfoResponse> responses = orders.stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());

        log.info("获取当前订单列表成功: count={}", responses.size());
        return responses;
    }

    @Override
    public PageResult<OrderInfoResponse> getHistoryOrders(OrderQueryRequest request) {
        log.info("获取历史订单列表: request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 状态筛选：如果未指定状态，默认查询所有已完成的订单
        if (StringUtils.isNotBlank(request.getStatus())) {
            if (!"all".equals(request.getStatus())) {
                wrapper.eq(Order::getStatus, request.getStatus());
            }
        } else {
            // 默认只查询已完成的订单
            wrapper.eq(Order::getStatus, "completed");
        }

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

        // 按开始时间倒序
        wrapper.orderByDesc(Order::getStartTime);

        // 分页查询
        Page<Order> page = new Page<>(request.getPage(), request.getPageSize());
        Page<Order> resultPage = orderMapper.selectPage(page, wrapper);

        // 转换结果
        List<OrderInfoResponse> records = resultPage.getRecords().stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());

        PageResult<OrderInfoResponse> pageResult = new PageResult<>();
        pageResult.setList(records);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage(request.getPage());
        pageResult.setPageSize(request.getPageSize());

        log.info("获取历史订单列表成功: total={}", pageResult.getTotal());
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
        response.setTableId(order.getTableId());
        response.setTableName(order.getTableName());
        response.setStartTime(order.getStartTime());
        response.setEndTime(order.getEndTime());
        response.setDuration(order.getDuration());
        response.setPauseDuration(order.getPauseDuration());
        response.setPresetDuration(order.getPresetDuration());
        response.setStatus(order.getStatus());
        response.setOperatorName(order.getOperatorName());
        response.setPaidAt(order.getPaidAt());
        response.setCreatedAt(order.getCreatedAt());

        // 计算当前费用
        int actualDuration = order.getDuration() - order.getPauseDuration();
        AmountDetail amountDetail = billingService.calculateAmount(actualDuration, order.getPresetDuration());
        response.setAmount(amountDetail.getTotalAmount());

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

        // 计算计费时长和费用
        int actualDuration = order.getDuration() - order.getPauseDuration();
        response.setActualDuration(actualDuration);

        AmountDetail amountDetail = billingService.calculateAmount(actualDuration, order.getPresetDuration());
        response.setNormalAmount(amountDetail.getNormalAmount());
        response.setOvertimeAmount(amountDetail.getOvertimeAmount());
        response.setAmount(amountDetail.getTotalAmount());
        response.setAmountDetailInfo(amountDetail);

        // 解析金额明细
        if (StringUtils.isNotBlank(order.getAmountDetail())) {
            try {
                JSONObject jsonObject = JSONUtil.parseObj(order.getAmountDetail());
                // 可以根据需要解析更多字段
            } catch (Exception e) {
                log.warn("解析金额明细失败: orderId={}, amountDetail={}", order.getId(), order.getAmountDetail());
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

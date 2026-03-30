package com.pindou.timer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.OrderDetailResponse;
import com.pindou.timer.dto.OrderItemResponse;
import com.pindou.timer.dto.OrderQueryRequest;
import com.pindou.timer.entity.Order;
import com.pindou.timer.mapper.OrderMapper;
import com.pindou.timer.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单Service实现类
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public List<OrderItemResponse> getActiveOrders() {
        log.info("获取活跃订单列表");

        // 查询活跃订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, "active")
                .orderByAsc(Order::getTableId);

        List<Order> orders = orderMapper.selectList(wrapper);

        // 转换为响应DTO
        List<OrderItemResponse> responses = orders.stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());

        log.info("获取活跃订单成功: count={}", responses.size());
        return responses;
    }

    @Override
    public PageResult<OrderItemResponse> getOrderHistory(OrderQueryRequest request) {
        log.info("查询历史订单: page={}, pageSize={}, status={}, tableId={}, startTime={}, endTime={}",
                request.getPage(), request.getPageSize(), request.getStatus(),
                request.getTableId(), request.getStartTime(), request.getEndTime());

        // 构建分页对象
        Page<Order> page = new Page<>(request.getPage(), request.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 状态筛选（默认查询已完成的订单）
        wrapper.eq(request.getStatus() != null, Order::getStatus,
                request.getStatus() != null ? request.getStatus() : "completed");

        // 桌台筛选
        wrapper.eq(request.getTableId() != null, Order::getTableId, request.getTableId());

        // 时间范围筛选
        if (request.getStartTime() != null) {
            wrapper.ge(Order::getCreatedAt, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(Order::getCreatedAt, request.getEndTime());
        }

        // 关键词搜索（桌台名称、操作员姓名）
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Order::getTableName, request.getKeyword())
                    .or()
                    .like(Order::getOperatorName, request.getKeyword()));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Order::getCreatedAt);

        // 分页查询
        Page<Order> resultPage = orderMapper.selectPage(page, wrapper);

        // 转换为响应DTO
        List<OrderItemResponse> list = resultPage.getRecords().stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());

        PageResult<OrderItemResponse> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage(request.getPage());
        pageResult.setPageSize(request.getPageSize());

        log.info("查询历史订单成功: total={}", resultPage.getTotal());
        return pageResult;
    }

    @Override
    public OrderDetailResponse getOrderDetail(String orderId) {
        log.info("获取订单详情: orderId={}", orderId);

        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new com.pindou.timer.common.exception.BusinessException(
                    com.pindou.timer.common.result.ErrorCode.ORDER_NOT_FOUND);
        }

        // 转换为详情响应
        OrderDetailResponse response = convertToDetailResponse(order);

        log.info("获取订单详情成功: orderId={}", orderId);
        return response;
    }

    @Override
    public List<OrderItemResponse> exportOrders(OrderQueryRequest request) {
        log.info("导出订单数据");

        // 设置较大的页面大小用于导出
        request.setPageSize(10000);
        request.setPage(1);

        // 查询订单
        PageResult<OrderItemResponse> pageResult = getOrderHistory(request);
        return pageResult.getList();
    }

    /**
     * 转换为列表项响应DTO
     */
    private OrderItemResponse convertToItemResponse(Order order) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(order.getId());
        response.setTableId(order.getTableId());
        response.setTableName(order.getTableName());
        response.setStartTime(order.getStartTime());
        response.setEndTime(order.getEndTime());
        response.setDuration(order.getDuration());
        response.setPresetDuration(order.getPresetDuration());
        response.setStatus(order.getStatus());
        response.setAmount(order.getAmount());
        response.setOperatorName(order.getOperatorName());
        response.setPaidAt(order.getPaidAt());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }

    /**
     * 转换为详情响应DTO
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

        // 计算实际使用时长
        int actualDuration = order.getDuration() - order.getPauseDuration();
        response.setActualDuration(actualDuration);

        response.setPresetDuration(order.getPresetDuration());
        response.setStatus(order.getStatus());
        response.setOperatorId(order.getOperatorId());
        response.setOperatorName(order.getOperatorName());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // 解析金额明细
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode amountDetail = mapper.readTree(order.getAmountDetail());
            response.setNormalAmount(new BigDecimal(amountDetail.get("normalFee").asText()));
            response.setOvertimeAmount(new BigDecimal(amountDetail.get("overtimeFee").asText()));
            response.setAmount(order.getAmount());
        } catch (Exception e) {
            log.warn("解析金额明细失败: orderId={}, error={}", order.getId(), e.getMessage());
            response.setNormalAmount(BigDecimal.ZERO);
            response.setOvertimeAmount(BigDecimal.ZERO);
            response.setAmount(order.getAmount());
        }

        // 构建时间线
        List<OrderDetailResponse.TimeLineItem> timeLine = new ArrayList<>();
        timeLine.add(createTimeLineItem(order.getStartTime(), "开始计时",
                "在" + order.getTableName() + "开始计时", order.getOperatorName()));

        if (order.getEndTime() != null) {
            timeLine.add(createTimeLineItem(order.getEndTime(), "结束结账",
                    "订单完成，总金额：" + order.getAmount() + "元", order.getOperatorName()));
        }

        if (order.getPaidAt() != null) {
            timeLine.add(createTimeLineItem(order.getPaidAt(), "支付完成",
                    "已完成支付", order.getOperatorName()));
        }

        response.setTimeLine(timeLine);

        return response;
    }

    /**
     * 创建时间线记录
     */
    private OrderDetailResponse.TimeLineItem createTimeLineItem(Long time, String action,
                                                                String description, String operator) {
        OrderDetailResponse.TimeLineItem item = new OrderDetailResponse.TimeLineItem();
        item.setTime(time);
        item.setAction(action);
        item.setDescription(description);
        item.setOperator(operator);
        return item;
    }
}

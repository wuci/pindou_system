package com.pindou.timer.service;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.OrderDetailResponse;
import com.pindou.timer.dto.OrderItemResponse;
import com.pindou.timer.dto.OrderQueryRequest;

import java.util.List;

/**
 * 订单Service接口
 *
 * @author pindou
 * @since 1.0.0
 */
public interface OrderService {

    /**
     * 获取当前活跃订单列表
     *
     * @return 活跃订单列表
     */
    List<OrderItemResponse> getActiveOrders();

    /**
     * 分页查询历史订单
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<OrderItemResponse> getOrderHistory(OrderQueryRequest request);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderDetailResponse getOrderDetail(String orderId);

    /**
     * 导出订单数据
     *
     * @param request 查询请求
     * @return 订单列表
     */
    List<OrderItemResponse> exportOrders(OrderQueryRequest request);
}

package com.pindou.timer.service;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.*;

import java.util.List;

/**
 * 订单Service接口
 *
 * @author wuci
 * @date 2026-03-28
 */
public interface OrderService {

    /**
     * 获取当前进行中的订单列表
     *
     * @return 当前订单列表
     */
    List<OrderInfoResponse> getActiveOrders();

    /**
     * 获取历史订单列表（分页）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<OrderInfoResponse> getHistoryOrders(OrderQueryRequest request);

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
    List<OrderInfoResponse> exportOrders(OrderQueryRequest request);
}

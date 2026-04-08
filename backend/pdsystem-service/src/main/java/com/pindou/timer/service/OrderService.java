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
     * 获取当天已完成订单列表（分页）
     *
     * @param page 页码，从1开始
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<OrderInfoResponse> getActiveOrders(Integer page, Integer pageSize);

    /**
     * 获取进行中订单列表（分页）
     *
     * @param page 页码，从1开始
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<OrderInfoResponse> getActiveOrdersNow(Integer page, Integer pageSize);

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

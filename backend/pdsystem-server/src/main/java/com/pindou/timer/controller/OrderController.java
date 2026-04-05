package com.pindou.timer.controller;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.OrderDetailResponse;
import com.pindou.timer.dto.OrderInfoResponse;
import com.pindou.timer.dto.OrderQueryRequest;
import com.pindou.timer.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 *
 * @author wuci
 * @date 2026-03-28
 */
@Tag(name = "订单接口", description = "订单管理相关接口")
@RestController
@RequestMapping("/api/orders")
public class OrderController extends ETSBaseController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 获取当天已完成的订单列表（分页）
     */
    @Operation(summary = "获取当天已完成订单列表")
    @GetMapping("/active")
    public Result<PageResult<OrderInfoResponse>> getActiveOrders(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

        log.info("获取当天已完成订单列表请求: page={}, pageSize={}", page, pageSize);

        PageResult<OrderInfoResponse> result = orderService.getActiveOrders(page, pageSize);

        return Result.success(result);
    }

    /**
     * 获取历史订单列表（分页）
     */
    @Operation(summary = "获取历史订单列表")
    @GetMapping("/history")
    public Result<PageResult<OrderInfoResponse>> getHistoryOrders(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer tableId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false) String keyword) {

        log.info("获取历史订单列表请求: page={}, pageSize={}, status={}, tableId={}, keyword={}",
                page, pageSize, status, tableId, keyword);

        OrderQueryRequest request = new OrderQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setStatus(status);
        request.setTableId(tableId);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setKeyword(keyword);

        PageResult<OrderInfoResponse> result = orderService.getHistoryOrders(request);

        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<OrderDetailResponse> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable("id") String orderId) {

        log.info("获取订单详情请求: orderId={}", orderId);

        OrderDetailResponse response = orderService.getOrderDetail(orderId);

        return Result.success(response);
    }

    /**
     * 导出订单数据
     */
    @Operation(summary = "导出订单数据")
    @GetMapping("/export")
    public Result<List<OrderInfoResponse>> exportOrders(
            @RequestParam(required = false) Integer tableId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false) String keyword) {

        log.info("导出订单请求: tableId={}, startTime={}, endTime={}, keyword={}",
                tableId, startTime, endTime, keyword);

        OrderQueryRequest request = new OrderQueryRequest();
        request.setStatus("completed");
        request.setTableId(tableId);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setKeyword(keyword);

        List<OrderInfoResponse> result = orderService.exportOrders(request);

        return Result.success(result);
    }
}

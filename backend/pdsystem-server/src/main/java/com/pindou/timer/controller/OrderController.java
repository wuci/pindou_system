package com.pindou.timer.controller;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.OrderDetailResponse;
import com.pindou.timer.dto.OrderItemResponse;
import com.pindou.timer.dto.OrderQueryRequest;
import com.pindou.timer.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "订单接口", description = "订单管理相关接口")
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 获取当前活跃订单列表
     */
    @Operation(summary = "获取当前活跃订单列表")
    @GetMapping("/active")
    public Result<List<OrderItemResponse>> getActiveOrders() {
        log.info("获取活跃订单列表请求");

        List<OrderItemResponse> orders = orderService.getActiveOrders();

        return Result.success(orders);
    }

    /**
     * 分页查询历史订单
     */
    @Operation(summary = "分页查询历史订单")
    @GetMapping("/history")
    public Result<PageResult<OrderItemResponse>> getOrderHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer tableId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false) String keyword) {

        log.info("查询历史订单请求: page={}, pageSize={}, status={}, tableId={}, startTime={}, endTime={}, keyword={}",
                page, pageSize, status, tableId, startTime, endTime, keyword);

        OrderQueryRequest request = new OrderQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setStatus(status != null ? status : "completed");  // 默认查询已完成订单
        request.setTableId(tableId);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setKeyword(keyword);

        PageResult<OrderItemResponse> result = orderService.getOrderHistory(request);

        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<OrderDetailResponse> getOrderDetail(@PathVariable("id") String orderId) {
        log.info("获取订单详情请求: orderId={}", orderId);

        OrderDetailResponse response = orderService.getOrderDetail(orderId);

        return Result.success(response);
    }

    /**
     * 导出订单数据
     */
    @Operation(summary = "导出订单数据")
    @GetMapping("/export")
    public Result<List<OrderItemResponse>> exportOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer tableId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false) String keyword) {

        log.info("导出订单请求: status={}, tableId={}, startTime={}, endTime={}, keyword={}",
                status, tableId, startTime, endTime, keyword);

        OrderQueryRequest request = new OrderQueryRequest();
        request.setStatus(status != null ? status : "completed");
        request.setTableId(tableId);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setKeyword(keyword);

        List<OrderItemResponse> orders = orderService.exportOrders(request);

        return Result.success(orders);
    }
}

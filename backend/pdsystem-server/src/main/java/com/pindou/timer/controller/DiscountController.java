package com.pindou.timer.controller;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.*;
import com.pindou.timer.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 折扣管理控制器
 *
 * @author wuci
 * @date 2026-04-06
 */
@Tag(name = "折扣管理接口", description = "折扣管理相关接口")
@RestController
@RequestMapping("/api/discounts")
public class DiscountController extends ETSBaseController {

    private static final Logger log = LoggerFactory.getLogger(DiscountController.class);

    @Resource
    private DiscountService discountService;

    /**
     * 获取折扣列表（分页）
     */
    @Operation(summary = "获取折扣列表")
    @GetMapping
    public Result<PageResult<DiscountResponse>> getDiscountList(DiscountQueryRequest request) {
        log.info("获取折扣列表请求: request={}", request);

        PageResult<DiscountResponse> result = discountService.getDiscountList(request);

        return Result.success(result);
    }

    /**
     * 获取所有启用的折扣列表
     */
    @Operation(summary = "获取所有启用的折扣")
    @GetMapping("/active")
    public Result<List<DiscountResponse>> getAllActiveDiscounts() {
        log.info("获取所有启用的折扣请求");

        List<DiscountResponse> discounts = discountService.getAllActiveDiscounts();

        return Result.success(discounts);
    }

    /**
     * 创建折扣
     */
    @Operation(summary = "创建折扣")
    @PostMapping
    public Result<Void> createDiscount(@Validated @RequestBody CreateDiscountRequest request) {
        log.info("创建折扣请求: name={}", request.getName());

        discountService.createDiscount(request);

        return Result.success();
    }

    /**
     * 更新折扣
     */
    @Operation(summary = "更新折扣")
    @PutMapping("/{id}")
    public Result<Void> updateDiscount(
            @Parameter(description = "折扣ID") @PathVariable("id") String discountId,
            @Validated @RequestBody UpdateDiscountRequest request) {

        log.info("更新折扣请求: discountId={}", discountId);

        discountService.updateDiscount(discountId, request);

        return Result.success();
    }

    /**
     * 删除折扣
     */
    @Operation(summary = "删除折扣")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDiscount(
            @Parameter(description = "折扣ID") @PathVariable("id") String discountId) {

        log.info("删除折扣请求: discountId={}", discountId);

        discountService.deleteDiscount(discountId);

        return Result.success();
    }

    /**
     * 启用/禁用折扣
     */
    @Operation(summary = "启用/禁用折扣")
    @PutMapping("/{id}/status")
    public Result<Void> updateDiscountStatus(
            @Parameter(description = "折扣ID") @PathVariable("id") String discountId,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {

        log.info("更新折扣状态请求: discountId={}, status={}", discountId, status);

        discountService.updateDiscountStatus(discountId, status);

        return Result.success();
    }

    /**
     * 计算订单折扣
     */
    @Operation(summary = "计算订单折扣")
    @PostMapping("/calculate")
    public Result<CalculateDiscountResponse> calculateDiscount(
            @Validated @RequestBody CalculateDiscountRequest request) {

        log.info("计算折扣请求: request={}", request);

        CalculateDiscountResponse response = discountService.calculateDiscount(request);

        return Result.success(response);
    }

    /**
     * 根据折扣ID计算折扣
     */
    @Operation(summary = "根据折扣ID计算折扣")
    @PostMapping("/calculate-by-id")
    public Result<CalculateDiscountResponse> calculateDiscountById(
            @Validated @RequestBody CalculateDiscountByIdRequest request) {

        log.info("根据折扣ID计算折扣: discountId={}, amount={}, memberId={}",
                request.getDiscountId(), request.getAmount(), request.getMemberId());

        CalculateDiscountResponse response = discountService.calculateDiscountById(
                request.getDiscountId(),
                request.getAmount(),
                request.getMemberId()
        );

        return Result.success(response);
    }
}

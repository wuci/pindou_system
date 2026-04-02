package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.BillingRuleConfigRequest;
import com.pindou.timer.dto.RemindConfigRequest;
import com.pindou.timer.dto.SystemConfigRequest;
import com.pindou.timer.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 配置控制器
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Tag(name = "配置接口", description = "系统配置相关接口")
@RestController
@RequestMapping("/config")
public class ConfigController {

    @Resource
    private ConfigService configService;

    /**
     * 获取所有配置
     */
    @Operation(summary = "获取所有配置")
    @GetMapping
    public Result<Map<String, String>> getAllConfigs() {
        log.info("获取所有配置请求");

        Map<String, String> configs = configService.getAllConfigs();

        return Result.success(configs);
    }

    /**
     * 获取计费规则配置
     */
    @Operation(summary = "获取计费规则")
    @GetMapping("/billing")
    public Result<String> getBillingRuleConfig() {
        log.info("获取计费规则配置请求");

        String config = configService.getBillingRuleConfig();

        return Result.success(config);
    }

    /**
     * 更新计费规则配置
     */
    @Operation(summary = "更新计费规则")
    @PutMapping("/billing")
    public Result<Boolean> updateBillingRuleConfig(@RequestBody BillingRuleConfigRequest request) {
        log.info("更新计费规则配置请求: request={}", request);

        Boolean result = configService.updateBillingRuleConfig(request);

        return Result.success(result);
    }

    /**
     * 获取提醒配置
     */
    @Operation(summary = "获取提醒配置")
    @GetMapping("/remind")
    public Result<String> getRemindConfig() {
        log.info("获取提醒配置请求");

        String config = configService.getRemindConfig();

        return Result.success(config);
    }

    /**
     * 更新提醒配置
     */
    @Operation(summary = "更新提醒配置")
    @PutMapping("/remind")
    public Result<Boolean> updateRemindConfig(@RequestBody RemindConfigRequest request) {
        log.info("更新提醒配置请求: request={}", request);

        Boolean result = configService.updateRemindConfig(request);

        return Result.success(result);
    }

    /**
     * 获取桌台数量配置
     */
    @Operation(summary = "获取桌台数量")
    @GetMapping("/table-count")
    public Result<Integer> getTableCountConfig() {
        log.info("获取桌台数量配置请求");

        Integer count = configService.getTableCountConfig();

        return Result.success(count);
    }

    /**
     * 更新桌台数量配置
     */
    @Operation(summary = "更新桌台数量")
    @PutMapping("/table-count")
    public Result<Boolean> updateTableCountConfig(@RequestParam Integer count) {
        log.info("更新桌台数量配置请求: count={}", count);

        Boolean result = configService.updateTableCountConfig(count);

        return Result.success(result);
    }

    /**
     * 获取系统参数配置
     */
    @Operation(summary = "获取系统参数配置")
    @GetMapping("/system")
    public Result<String> getSystemConfig() {
        log.info("获取系统参数配置请求");

        String config = configService.getSystemConfig();

        return Result.success(config);
    }

    /**
     * 更新系统参数配置
     */
    @Operation(summary = "更新系统参数配置")
    @PutMapping("/system")
    public Result<Boolean> updateSystemConfig(@Validated @RequestBody SystemConfigRequest request) {
        log.info("更新系统参数配置请求: request={}", request);

        Boolean result = configService.updateSystemConfig(request);

        return Result.success(result);
    }
}

package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.DesignStoreRulesRequest;
import com.pindou.timer.dto.DesignStoreRulesResponse;
import com.pindou.timer.service.DesignStoreRulesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 店铺规则控制器
 *
 * @author wuci
 * @date 2026-04-06
 */
@Tag(name = "店铺规则接口", description = "店铺规则管理相关接口")
@Slf4j
@RestController
@RequestMapping("/api/store-rules")
public class DesignStoreRulesController extends ETSBaseController {

    @Resource
    private DesignStoreRulesService designStoreRulesService;

    /**
     * 根据分类获取规则列表
     */
    @Operation(summary = "根据分类获取规则列表")
    @GetMapping
    public Result<List<DesignStoreRulesResponse>> getRulesByCategory(
            @RequestParam(required = false) String category) {
        log.info("根据分类获取规则列表请求: category={}", category);

        List<DesignStoreRulesResponse> rules = designStoreRulesService.getRulesByCategory(category);

        return Result.success(rules);
    }

    /**
     * 根据ID获取规则
     */
    @Operation(summary = "根据ID获取规则")
    @GetMapping("/{id}")
    public Result<DesignStoreRulesResponse> getRuleById(@PathVariable String id) {
        log.info("根据ID获取规则请求: id={}", id);

        DesignStoreRulesResponse rule = designStoreRulesService.getRuleById(id);

        return Result.success(rule);
    }

    /**
     * 创建规则
     */
    @Operation(summary = "创建规则")
    @PostMapping
    public Result<DesignStoreRulesResponse> createRule(
            @Validated @RequestBody DesignStoreRulesRequest request) {
        log.info("创建规则请求: request={}", request);

        DesignStoreRulesResponse rule = designStoreRulesService.createRule(request);

        return Result.success(rule);
    }

    /**
     * 更新规则
     */
    @Operation(summary = "更新规则")
    @PutMapping
    public Result<DesignStoreRulesResponse> updateRule(
            @Validated @RequestBody DesignStoreRulesRequest request) {
        log.info("更新规则请求: request={}", request);

        DesignStoreRulesResponse rule = designStoreRulesService.updateRule(request);

        return Result.success(rule);
    }

    /**
     * 删除规则
     */
    @Operation(summary = "删除规则")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRule(@PathVariable String id) {
        log.info("删除规则请求: id={}", id);

        Boolean result = designStoreRulesService.deleteRule(id);

        return Result.success(result);
    }

    /**
     * 切换规则启用状态
     */
    @Operation(summary = "切换规则启用状态")
    @PutMapping("/{id}/toggle")
    public Result<Boolean> toggleRuleEnabled(@PathVariable String id) {
        log.info("切换规则启用状态请求: id={}", id);

        Boolean result = designStoreRulesService.toggleRuleEnabled(id);

        return Result.success(result);
    }
}

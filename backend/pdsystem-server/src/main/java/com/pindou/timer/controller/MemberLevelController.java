package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.*;
import com.pindou.timer.service.MemberLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员等级控制器
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "会员等级接口", description = "会员等级相关接口")
@RestController
@RequestMapping("/member-levels")
public class MemberLevelController {

    private final MemberLevelService memberLevelService;

    public MemberLevelController(MemberLevelService memberLevelService) {
        this.memberLevelService = memberLevelService;
    }

    /**
     * 获取所有会员等级
     */
    @Operation(summary = "获取所有会员等级")
    @GetMapping
    public Result<List<MemberLevelResponse>> getAllLevels() {
        log.info("获取所有会员等级请求");

        List<MemberLevelResponse> levels = memberLevelService.getAllLevels();

        return Result.success(levels);
    }

    /**
     * 创建会员等级
     */
    @Operation(summary = "创建会员等级")
    @PostMapping
    public Result<Long> createLevel(@Validated @RequestBody CreateMemberLevelRequest request) {
        log.info("创建会员等级请求: name={}", request.getName());

        Long levelId = memberLevelService.createLevel(request);

        return Result.success(levelId);
    }

    /**
     * 更新会员等级
     */
    @Operation(summary = "更新会员等级")
    @PutMapping("/{id}")
    public Result<Void> updateLevel(
            @Parameter(description = "会员等级ID") @PathVariable("id") Long levelId,
            @Validated @RequestBody UpdateMemberLevelRequest request) {

        log.info("更新会员等级请求: levelId={}", levelId);

        memberLevelService.updateLevel(levelId, request);

        return Result.success();
    }

    /**
     * 删除会员等级
     */
    @Operation(summary = "删除会员等级")
    @DeleteMapping("/{id}")
    public Result<Void> deleteLevel(
            @Parameter(description = "会员等级ID") @PathVariable("id") Long levelId) {

        log.info("删除会员等级请求: levelId={}", levelId);

        memberLevelService.deleteLevel(levelId);

        return Result.success();
    }

    /**
     * 初始化默认会员等级
     */
    @Operation(summary = "初始化默认会员等级")
    @PostMapping("/init")
    public Result<String> initDefaultLevels() {
        log.info("初始化默认会员等级请求");

        int count = memberLevelService.initDefaultLevels();

        return Result.success("初始化完成，共创建" + count + "个默认等级");
    }
}

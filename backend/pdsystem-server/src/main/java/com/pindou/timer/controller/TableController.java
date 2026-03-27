package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.StartTimerRequest;
import com.pindou.timer.dto.TableConfigRequest;
import com.pindou.timer.dto.TableInfoResponse;
import com.pindou.timer.service.TableService;
import com.pindou.timer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 桌台控制器
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "桌台接口", description = "桌台管理相关接口")
@RestController
@RequestMapping("/tables")
public class TableController {

    private final TableService tableService;
    private final UserService userService;

    public TableController(TableService tableService, UserService userService) {
        this.tableService = tableService;
        this.userService = userService;
    }

    /**
     * 获取桌台列表
     */
    @Operation(summary = "获取桌台列表")
    @GetMapping
    public Result<List<TableInfoResponse>> getTableList(
            @RequestParam(required = false) String status) {

        log.info("获取桌台列表请求: status={}", status);

        List<TableInfoResponse> tables = tableService.getTableList(status);

        return Result.success(tables);
    }

    /**
     * 配置桌台数量
     */
    @Operation(summary = "配置桌台数量")
    @PutMapping("/config")
    public Result<Void> configTableCount(
            @Validated @RequestBody TableConfigRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // 获取当前用户信息（可选，用于记录操作日志）
        String userId = "";
        String username = "system";
        if (authHeader != null && authHeader.length() > 7) {
            try {
                String token = authHeader.substring(7);
                userId = userService.getUserIdFromToken(token);
                // 这里可以获取用户名，简化起见先用userId
                username = userId;
            } catch (Exception e) {
                // Token无效，使用system作为默认值
            }
        }

        log.info("配置桌台数量请求: tableCount={}, userId={}", request.getTableCount(), userId);

        tableService.configTableCount(request, userId, username);

        return Result.success();
    }

    /**
     * 开始计时
     */
    @Operation(summary = "开始计时")
    @PostMapping("/{id}/start")
    public Result<TableInfoResponse> startTimer(
            @PathVariable("id") Integer tableId,
            @Validated @RequestBody StartTimerRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // 获取当前用户信息
        String userId = "";
        String username = "system";
        if (authHeader != null && authHeader.length() > 7) {
            try {
                String token = authHeader.substring(7);
                userId = userService.getUserIdFromToken(token);
                username = userId;
            } catch (Exception e) {
                // Token无效，使用system作为默认值
            }
        }

        log.info("开始计时请求: tableId={}, presetDuration={}, userId={}", tableId, request.getPresetDuration(), userId);

        TableInfoResponse response = tableService.startTimer(tableId, request, userId, username);

        return Result.success(response);
    }

    /**
     * 暂停计时
     */
    @Operation(summary = "暂停计时")
    @PostMapping("/{id}/pause")
    public Result<Void> pauseTimer(
            @PathVariable("id") Integer tableId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // 获取当前用户信息
        String userId = "";
        String username = "system";
        if (authHeader != null && authHeader.length() > 7) {
            try {
                String token = authHeader.substring(7);
                userId = userService.getUserIdFromToken(token);
                username = userId;
            } catch (Exception e) {
                // Token无效，使用system作为默认值
            }
        }

        log.info("暂停计时请求: tableId={}, userId={}", tableId, userId);

        tableService.pauseTimer(tableId, userId, username);

        return Result.success();
    }

    /**
     * 恢复计时
     */
    @Operation(summary = "恢复计时")
    @PostMapping("/{id}/resume")
    public Result<Void> resumeTimer(
            @PathVariable("id") Integer tableId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // 获取当前用户信息
        String userId = "";
        String username = "system";
        if (authHeader != null && authHeader.length() > 7) {
            try {
                String token = authHeader.substring(7);
                userId = userService.getUserIdFromToken(token);
                username = userId;
            } catch (Exception e) {
                // Token无效，使用system作为默认值
            }
        }

        log.info("恢复计时请求: tableId={}, userId={}", tableId, userId);

        tableService.resumeTimer(tableId, userId, username);

        return Result.success();
    }

    /**
     * 结束计时并结账
     */
    @Operation(summary = "结束计时并结账")
    @PostMapping("/{id}/end")
    public Result<Void> endTimer(
            @PathVariable("id") Integer tableId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // 获取当前用户信息
        String userId = "";
        String username = "system";
        if (authHeader != null && authHeader.length() > 7) {
            try {
                String token = authHeader.substring(7);
                userId = userService.getUserIdFromToken(token);
                username = userId;
            } catch (Exception e) {
                // Token无效，使用system作为默认值
            }
        }

        log.info("结束计时请求: tableId={}, userId={}", tableId, userId);

        tableService.endTimer(tableId, userId, username);

        return Result.success();
    }

    /**
     * 忽略提醒
     */
    @Operation(summary = "忽略提醒")
    @PostMapping("/{id}/ignore-remind")
    public Result<Void> ignoreRemind(
            @PathVariable("id") Integer tableId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // 获取当前用户信息
        String userId = "";
        String username = "system";
        if (authHeader != null && authHeader.length() > 7) {
            try {
                String token = authHeader.substring(7);
                userId = userService.getUserIdFromToken(token);
                username = userId;
            } catch (Exception e) {
                // Token无效，使用system作为默认值
            }
        }

        log.info("忽略提醒请求: tableId={}, userId={}", tableId, userId);

        tableService.ignoreRemind(tableId, userId, username);

        return Result.success();
    }
}

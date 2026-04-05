package com.pindou.timer.controller;

import com.pindou.timer.annotation.LogOperation;
import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.BillResponse;
import com.pindou.timer.dto.EndTableRequest;
import com.pindou.timer.dto.ExtendTableRequest;
import com.pindou.timer.dto.ReservationRequest;
import com.pindou.timer.dto.StartTimerRequest;
import com.pindou.timer.dto.TableConfigRequest;
import com.pindou.timer.dto.TableInfoResponse;
import com.pindou.timer.dto.UpdateTableRequest;
import com.pindou.timer.service.TableService;
import com.pindou.timer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 桌台控制器
 *
 * @author pindou
 * @since 1.0.0
 */
@Tag(name = "桌台接口", description = "桌台管理相关接口")
@RestController
@RequestMapping("/api/tables")
public class TableController extends ETSBaseController {

    private static final Logger log = LoggerFactory.getLogger(TableController.class);

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
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name) {

        log.info("获取桌台列表请求: status={}, categoryId={}, name={}", status, categoryId, name);

        List<TableInfoResponse> tables = tableService.getTableList(status, categoryId, name);

        return Result.success(tables);
    }

    /**
     * 更新桌台信息
     */
    @Operation(summary = "更新桌台信息")
    @LogOperation(
        module = "桌台管理",
        operation = "更新桌台",
        description = "更新桌台【#tableId】信息，名称【#request.name】"
    )
    @PutMapping("/{id}")
    public Result<Void> updateTable(
            @PathVariable("id") Integer tableId,
            @RequestBody UpdateTableRequest request) {

        log.info("更新桌台信息请求: tableId={}, name={}", tableId, request.getName());

        request.setId(tableId);
        tableService.updateTable(request);

        return Result.success();
    }

    /**
     * 配置桌台数量
     */
    @Operation(summary = "配置桌台数量")
    @LogOperation(
        module = "桌台管理",
        operation = "配置桌台",
        description = "配置桌台数量为【#request.tableCount】个"
    )
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
    @LogOperation(
        module = "桌台管理",
        operation = "开始计时",
        description = "开始计时",
        saveParams = true
    )
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
    @LogOperation(
        module = "桌台管理",
        operation = "暂停计时",
        description = "暂停桌台【#tableId】计时"
    )
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
    @LogOperation(
        module = "桌台管理",
        operation = "恢复计时",
        description = "恢复桌台【#tableId】计时"
    )
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
     * 续费时长
     */
    @Operation(summary = "续费时长")
    @LogOperation(
        module = "桌台管理",
        operation = "续费时长",
        description = "桌台【#tableId】续费【#request.additionalDuration】秒，渠道【#request.channel】"
    )
    @PostMapping("/{id}/extend")
    public Result<TableInfoResponse> extendTimer(
            @PathVariable("id") Integer tableId,
            @Validated @RequestBody ExtendTableRequest request,
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

        log.info("续费时长请求: tableId={}, additionalDuration={}, userId={}", tableId, request.getAdditionalDuration(), userId);

        TableInfoResponse response = tableService.extendTimer(tableId, request, userId, username);

        return Result.success(response);
    }

    /**
     * 结束计时并结账
     */
    @Operation(summary = "结束计时并结账")
    @LogOperation(
        module = "桌台管理",
        operation = "结束计时",
        description = "桌台【#tableId】结束计时并结账，会员ID【#request?.memberId】"
    )
    @PostMapping("/{id}/end")
    public Result<Void> endTimer(
            @PathVariable("id") Integer tableId,
            @RequestBody(required = false) EndTableRequest request,
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

        log.info("结束计时请求: tableId={}, userId={}, memberId={}",
                tableId, userId, request != null ? request.getMemberId() : null);

        tableService.endTimer(tableId, request, userId, username);

        return Result.success();
    }

    /**
     * 忽略提醒
     */
    @Operation(summary = "忽略提醒")
    @LogOperation(
        module = "桌台管理",
        operation = "忽略提醒",
        description = "忽略桌台【#tableId】的提醒"
    )
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

    /**
     * 获取桌台账单
     */
    @Operation(summary = "获取桌台账单")
    @GetMapping("/{id}/bill")
    public Result<BillResponse> getBill(@PathVariable("id") Integer tableId) {
        log.info("获取桌台账单请求: tableId={}", tableId);

        BillResponse bill = tableService.getBill(tableId);

        return Result.success(bill);
    }

    /**
     * 删除桌台
     */
    @Operation(summary = "删除桌台")
    @LogOperation(
        module = "桌台管理",
        operation = "删除桌台",
        description = "删除桌台【#tableId】"
    )
    @DeleteMapping("/{id}")
    public Result<Void> deleteTable(@PathVariable("id") Integer tableId) {
        log.info("删除桌台请求: tableId={}", tableId);

        tableService.deleteTable(tableId);

        return Result.success();
    }

    /**
     * 批量删除桌台
     */
    @Operation(summary = "批量删除桌台")
    @LogOperation(
        module = "桌台管理",
        operation = "批量删除",
        description = "批量删除桌台【#request.tableIds】"
    )
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteTables(@RequestBody com.pindou.timer.dto.BatchDeleteTableRequest request) {
        log.info("批量删除桌台请求: tableIds={}", request.getTableIds());

        tableService.batchDeleteTables(request.getTableIds());

        return Result.success();
    }

    /**
     * 创建桌台预定
     */
    @Operation(summary = "创建桌台预定")
    @LogOperation(
        module = "桌台管理",
        operation = "创建预定",
        description = "桌台【#tableId】预定，预定人【#request.reservationName】"
    )
    @PostMapping("/{id}/reservation")
    public Result<Void> createReservation(
            @PathVariable("id") Integer tableId,
            @Validated @RequestBody ReservationRequest request,
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

        log.info("创建桌台预定请求: tableId={}, reservationName={}, userId={}",
                tableId, request.getReservationName(), userId);

        tableService.createReservation(tableId, request, userId, username);

        return Result.success();
    }

    /**
     * 取消桌台预定
     */
    @Operation(summary = "取消桌台预定")
    @LogOperation(
        module = "桌台管理",
        operation = "取消预定",
        description = "取消桌台【#tableId】的预定"
    )
    @DeleteMapping("/{id}/reservation")
    public Result<Void> cancelReservation(
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

        log.info("取消桌台预定请求: tableId={}, userId={}", tableId, userId);

        tableService.cancelReservation(tableId, userId, username);

        return Result.success();
    }
}

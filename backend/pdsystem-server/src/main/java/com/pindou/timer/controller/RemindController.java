package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.RemindInfo;
import com.pindou.timer.service.RemindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 提醒控制器
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Tag(name = "提醒接口", description = "提醒系统相关接口")
@RestController
@RequestMapping("/remind")
public class RemindController {

    @Resource
    private RemindService remindService;

    /**
     * 获取需要提醒的桌台列表
     */
    @Operation(summary = "获取提醒列表")
    @GetMapping
    public Result<List<RemindInfo>> getReminders() {
        log.info("获取提醒列表请求");

        List<RemindInfo> reminders = remindService.checkReminders();

        return Result.success(reminders);
    }

    /**
     * 忽略桌台提醒
     */
    @Operation(summary = "忽略提醒")
    @PostMapping("/ignore/{tableId}")
    public Result<Boolean> ignoreRemind(@PathVariable("tableId") Integer tableId) {
        log.info("忽略提醒请求: tableId={}", tableId);

        Boolean result = remindService.ignoreRemind(tableId);

        return Result.success(result);
    }
}

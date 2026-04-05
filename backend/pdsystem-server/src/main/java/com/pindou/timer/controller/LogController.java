package com.pindou.timer.controller;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.LogInfoResponse;
import com.pindou.timer.dto.LogQueryRequest;
import com.pindou.timer.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 日志控制器
 *
 * @author wuci
 * @date 2026-03-28
 */
@Tag(name = "日志接口", description = "操作日志相关接口")
@RestController
@RequestMapping("/api/logs")
public class LogController extends ETSBaseController {

    private static final Logger log = LoggerFactory.getLogger(LogController.class);

    @Resource
    private LogService logService;

    /**
     * 获取日志列表（分页）
     */
    @Operation(summary = "获取日志列表")
    @GetMapping
    public Result<PageResult<LogInfoResponse>> getLogList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false) Integer status) {

        log.info("获取日志列表请求: page={}, pageSize={}, module={}, operation={}, username={}",
                page, pageSize, module, operation, username);

        LogQueryRequest request = new LogQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setModule(module);
        request.setOperation(operation);
        request.setUsername(username);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(status);

        PageResult<LogInfoResponse> result = logService.getLogList(request);

        return Result.success(result);
    }

    /**
     * 导出日志数据
     */
    @Operation(summary = "导出日志")
    @GetMapping("/export")
    public Result<List<LogInfoResponse>> exportLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {

        log.info("导出日志请求: module={}, operation={}, startTime={}, endTime={}",
                module, operation, startTime, endTime);

        LogQueryRequest request = new LogQueryRequest();
        request.setModule(module);
        request.setOperation(operation);
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        List<LogInfoResponse> result = logService.exportLogs(request);

        return Result.success(result);
    }
}

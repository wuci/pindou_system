package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.DailyStatsResponse;
import com.pindou.timer.dto.RevenueTrendItem;
import com.pindou.timer.dto.RevenueTrendRequest;
import com.pindou.timer.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 统计控制器
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Tag(name = "统计接口", description = "数据统计相关接口")
@RestController
@RequestMapping("/reports")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * 获取今日统计数据
     */
    @Operation(summary = "获取今日统计")
    @GetMapping("/daily")
    public Result<DailyStatsResponse> getDailyStats() {
        log.info("获取今日统计请求");

        DailyStatsResponse stats = statisticsService.getDailyStats();

        return Result.success(stats);
    }

    /**
     * 获取营收趋势数据
     */
    @Operation(summary = "获取营收趋势")
    @GetMapping("/trend")
    public Result<List<RevenueTrendItem>> getRevenueTrend(
            @RequestParam(required = false, defaultValue = "7") Integer days) {

        log.info("获取营收趋势请求: days={}", days);

        RevenueTrendRequest request = new RevenueTrendRequest();
        request.setDays(days);

        List<RevenueTrendItem> trend = statisticsService.getRevenueTrend(request);

        return Result.success(trend);
    }
}

package com.pindou.timer.service;

import com.pindou.timer.dto.DailyStatsResponse;
import com.pindou.timer.dto.RevenueTrendItem;
import com.pindou.timer.dto.RevenueTrendRequest;

import java.util.List;

/**
 * 统计Service接口
 *
 * @author wuci
 * @date 2026-03-28
 */
public interface StatisticsService {

    /**
     * 获取今日统计数据
     *
     * @return 今日统计数据
     */
    DailyStatsResponse getDailyStats();

    /**
     * 获取营收趋势数据
     *
     * @param request 查询请求
     * @return 营收趋势数据
     */
    List<RevenueTrendItem> getRevenueTrend(RevenueTrendRequest request);
}

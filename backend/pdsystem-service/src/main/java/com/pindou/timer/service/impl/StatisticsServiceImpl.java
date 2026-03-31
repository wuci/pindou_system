package com.pindou.timer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.dto.DailyStatsResponse;
import com.pindou.timer.dto.RevenueTrendItem;
import com.pindou.timer.dto.RevenueTrendRequest;
import com.pindou.timer.entity.Order;
import com.pindou.timer.entity.Table;
import com.pindou.timer.mapper.OrderMapper;
import com.pindou.timer.mapper.TableMapper;
import com.pindou.timer.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 统计Service实现类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private TableMapper tableMapper;

    /**
     * 获取今天的起始时间（0点0分0秒）
     */
    private long getTodayStartTime() {
        return System.currentTimeMillis() / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000) - 8 * 60 * 60 * 1000;
    }

    @Override
    public DailyStatsResponse getDailyStats() {
        log.info("获取今日统计数据");

        long todayStartTime = getTodayStartTime();

        // 1. 今日营收
        LambdaQueryWrapper<Order> revenueWrapper = new LambdaQueryWrapper<>();
        revenueWrapper.eq(Order::getStatus, "completed")
                .ge(Order::getPaidAt, todayStartTime);
        List<Order> todayOrders = orderMapper.selectList(revenueWrapper);

        double todayRevenue = todayOrders.stream()
                .mapToDouble(order -> {
                    // 计算实际费用
                    int actualDuration = order.getDuration() - order.getPauseDuration();
                    // 这里简化处理，直接返回订单金额（实际应该调用计费服务）
                    return 0.0;
                })
                .sum();

        // 从订单中获取已支付金额
        LambdaQueryWrapper<Order> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.eq(Order::getStatus, "completed")
                .ge(Order::getPaidAt, todayStartTime);
        List<Order> paidOrders = orderMapper.selectList(paidWrapper);

        todayRevenue = paidOrders.stream()
                .map(order -> order.getAmount() != null ? order.getAmount().doubleValue() : 0.0)
                .mapToDouble(Double::doubleValue)
                .sum();

        // 2. 使用中桌台数
        LambdaQueryWrapper<Table> tableWrapper = new LambdaQueryWrapper<>();
        tableWrapper.eq(Table::getStatus, "using");
        Long activeCount = tableMapper.selectCount(tableWrapper);

        // 3. 今日订单数
        Integer todayOrderCount = todayOrders.size();

        // 4. 总桌台数（用于计算翻台率）
        LambdaQueryWrapper<Table> totalTableWrapper = new LambdaQueryWrapper<>();
        Long totalTableCount = tableMapper.selectCount(totalTableWrapper);

        // 5. 翻台率 = 订单数 / 桌台数
        double turnoverRate = 0.0;
        if (totalTableCount != null && totalTableCount > 0) {
            turnoverRate = (double) todayOrderCount / totalTableCount;
        }

        // 6. 今日总时长
        Long todayDuration = todayOrders.stream()
                .mapToLong(Order::getDuration)
                .sum();

        DailyStatsResponse response = new DailyStatsResponse();
        response.setTodayRevenue(todayRevenue);
        response.setActiveTableCount(activeCount != null ? activeCount.intValue() : 0);
        response.setTodayOrderCount(todayOrderCount);
        response.setTurnoverRate(turnoverRate);
        response.setTodayDuration(todayDuration);

        log.info("获取今日统计数据成功: revenue={}, orders={}, tables={}",
                todayRevenue, todayOrderCount, activeCount);
        return response;
    }

    @Override
    public List<RevenueTrendItem> getRevenueTrend(RevenueTrendRequest request) {
        log.info("获取营收趋势数据: request={}", request);

        int days = request.getDays() != null && request.getDays() > 0 ? request.getDays() : 7;
        days = Math.min(days, 30); // 最多查询30天

        List<RevenueTrendItem> result = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -i);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long dayStart = calendar.getTimeInMillis();
            long dayEnd = dayStart + 24 * 60 * 60 * 1000 - 1;

            // 查询当天已完成的订单
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, "completed")
                    .ge(Order::getPaidAt, dayStart)
                    .le(Order::getPaidAt, dayEnd);

            List<Order> dayOrders = orderMapper.selectList(wrapper);

            double revenue = dayOrders.stream()
                    .map(order -> order.getAmount() != null ? order.getAmount().doubleValue() : 0.0)
                    .mapToDouble(Double::doubleValue)
                    .sum();

            int orderCount = dayOrders.size();

            RevenueTrendItem item = new RevenueTrendItem();
            item.setDate(dateFormat.format(new Date(dayStart)));
            item.setRevenue(revenue);
            item.setOrderCount(orderCount);

            result.add(item);
        }

        log.info("获取营收趋势数据成功: count={}", result.size());
        return result;
    }
}

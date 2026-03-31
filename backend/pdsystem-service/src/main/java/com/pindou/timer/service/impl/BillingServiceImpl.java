package com.pindou.timer.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.dto.AmountDetail;
import com.pindou.timer.entity.Config;
import com.pindou.timer.entity.Order;
import com.pindou.timer.mapper.ConfigMapper;
import com.pindou.timer.mapper.OrderMapper;
import com.pindou.timer.service.BillingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 计费服务实现类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Service
public class BillingServiceImpl implements BillingService {

    /**
     * 计费规则配置键
     */
    private static final String BILLING_RULE_KEY = "billing_rule";

    /**
     * 默认每小时单价（元）
     */
    private static final Double DEFAULT_PRICE_PER_HOUR = 30.0;

    /**
     * 默认每分钟单价（元）
     */
    private static final Double DEFAULT_PRICE_PER_MINUTE = 0.5;

    /**
     * 默认超时费率
     */
    private static final Double DEFAULT_OVERTIME_RATE = 1.5;

    /**
     * 一小时秒数
     */
    private static final int HOUR_SECONDS = 3600;

    /**
     * 一分钟秒数
     */
    private static final int MINUTE_SECONDS = 60;

    @Resource
    private ConfigMapper configMapper;

    @Resource
    private OrderMapper orderMapper;

    @Override
    public com.pindou.timer.dto.BillingRule getBillingRule() {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getConfigKey, BILLING_RULE_KEY);
        Config config = configMapper.selectOne(wrapper);

        if (config == null) {
            log.warn("计费规则配置不存在，使用默认配置");
            return getDefaultBillingRule();
        }

        try {
            JSONObject jsonObject = JSONUtil.parseObj(config.getConfigValue());
            com.pindou.timer.dto.BillingRule rule = new com.pindou.timer.dto.BillingRule();
            rule.setType(jsonObject.getStr("type"));
            rule.setPricePerHour(jsonObject.getDouble("pricePerHour"));
            rule.setPricePerMinute(jsonObject.getDouble("pricePerMinute"));
            rule.setOvertimeRate(jsonObject.getDouble("overtimeRate"));
            return rule;
        } catch (Exception e) {
            log.error("解析计费规则配置失败：{}", e.getMessage());
            return getDefaultBillingRule();
        }
    }

    @Override
    public AmountDetail calculateAmount(Integer actualDuration, Integer presetDuration) {
        if (actualDuration == null || actualDuration <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "计费时长不能为空或小于等于0");
        }

        com.pindou.timer.dto.BillingRule rule = getBillingRule();
        AmountDetail detail = new AmountDetail();

        detail.setActualDuration(actualDuration);
        detail.setBillingType(rule.getType());
        detail.setUnitPrice("hour".equals(rule.getType()) ? rule.getPricePerHour() : rule.getPricePerMinute());
        detail.setOvertimeRate(rule.getOvertimeRate());

        // 判断是否超时
        boolean isOvertime = presetDuration != null && actualDuration > presetDuration;

        double normalAmount = 0.0;
        double overtimeAmount = 0.0;

        if ("hour".equals(rule.getType())) {
            // 按小时计费
            if (isOvertime) {
                // 有超时
                normalAmount = calculateByHour(presetDuration, rule.getPricePerHour());
                int overtimeDuration = actualDuration - presetDuration;
                overtimeAmount = calculateByHour(overtimeDuration, rule.getPricePerHour() * rule.getOvertimeRate());
            } else {
                // 无超时
                normalAmount = calculateByHour(actualDuration, rule.getPricePerHour());
            }
        } else {
            // 按分钟计费
            if (isOvertime) {
                // 有超时
                normalAmount = calculateByMinute(presetDuration, rule.getPricePerMinute());
                int overtimeDuration = actualDuration - presetDuration;
                overtimeAmount = calculateByMinute(overtimeDuration, rule.getPricePerMinute() * rule.getOvertimeRate());
            } else {
                // 无超时
                normalAmount = calculateByMinute(actualDuration, rule.getPricePerMinute());
            }
        }

        detail.setNormalAmount(roundAmount(normalAmount));
        detail.setOvertimeAmount(roundAmount(overtimeAmount));
        detail.setTotalAmount(roundAmount(normalAmount + overtimeAmount));

        return detail;
    }

    @Override
    public AmountDetail recalculateAmount(String orderId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getId, orderId);
        Order order = orderMapper.selectOne(wrapper);

        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }

        // 计算计费时长 = 总时长 - 暂停时长
        int actualDuration = order.getDuration() - order.getPauseDuration();
        return calculateAmount(actualDuration, order.getPresetDuration());
    }

    /**
     * 按小时计算费用
     *
     * @param duration 时长（秒）
     * @param pricePerHour 每小时单价
     * @return 费用
     */
    private double calculateByHour(int duration, double pricePerHour) {
        // 转换为小时，向上取整
        double hours = Math.ceil((double) duration / HOUR_SECONDS);
        return hours * pricePerHour;
    }

    /**
     * 按分钟计算费用
     *
     * @param duration 时长（秒）
     * @param pricePerMinute 每分钟单价
     * @return 费用
     */
    private double calculateByMinute(int duration, double pricePerMinute) {
        // 转换为分钟，向上取整
        double minutes = Math.ceil((double) duration / MINUTE_SECONDS);
        return minutes * pricePerMinute;
    }

    /**
     * 金额保留两位小数
     *
     * @param amount 金额
     * @return 保留两位小数后的金额
     */
    private double roundAmount(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    /**
     * 获取默认计费规则
     *
     * @return 默认计费规则
     */
    private com.pindou.timer.dto.BillingRule getDefaultBillingRule() {
        com.pindou.timer.dto.BillingRule rule = new com.pindou.timer.dto.BillingRule();
        rule.setType("hour");
        rule.setPricePerHour(DEFAULT_PRICE_PER_HOUR);
        rule.setPricePerMinute(DEFAULT_PRICE_PER_MINUTE);
        rule.setOvertimeRate(DEFAULT_OVERTIME_RATE);
        return rule;
    }
}

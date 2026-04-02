package com.pindou.timer.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.common.exception.BusinessException;
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
     * 默认超时费率（倍数）
     */
    private static final Double DEFAULT_OVERTIME_RATE = 1.5;

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
        // 兼容旧方法，使用默认渠道
        return calculateAmount("store", actualDuration, presetDuration);
    }

    @Override
    public AmountDetail calculateAmount(String orderId) {
        // 获取订单信息
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getId, orderId);
        Order order = orderMapper.selectOne(wrapper);

        if (order == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }

        // 计算实际使用时长（总时长 - 暂停时长）
        int actualDuration = order.getDuration() - order.getPauseDuration();

        return calculateAmount(order.getChannel(), actualDuration, order.getPresetDuration());
    }

    @Override
    public AmountDetail calculateAmount(String channel, Integer actualDuration, Integer presetDuration) {
        log.info("计算费用: channel={}, actualDuration={}秒, presetDuration={}秒", channel, actualDuration, presetDuration);

        // 获取计费规则
        String billingRuleConfig = getBillingRuleConfig();

        // 解析计费规则
        JSONObject billingRuleJson = JSONUtil.parseObj(billingRuleConfig);
        Object channelsObj = billingRuleJson.get("channels");

        if (channelsObj == null) {
            log.warn("计费规则中没有channels字段");
            return calculateByDefaultRule(actualDuration, presetDuration);
        }

        // 查找对应渠道的规则
        JSONObject channelRule = null;
        for (Object channelObj : (Iterable) channelsObj) {
            JSONObject ch = JSONUtil.parseObj(channelObj);
            if (channel != null && channel.equals(ch.getStr("channel"))) {
                channelRule = ch;
                break;
            }
        }

        if (channelRule == null) {
            log.warn("未找到渠道 {} 的计费规则，使用默认规则", channel);
            return calculateByDefaultRule(actualDuration, presetDuration);
        }

        // 获取规则列表
        Object rulesObj = channelRule.get("rules");
        if (rulesObj == null) {
            log.warn("渠道 {} 没有配置计费规则", channel);
            return calculateByDefaultRule(actualDuration, presetDuration);
        }

        // 计算费用
        return calculateByRules(actualDuration, presetDuration, rulesObj);
    }

    @Override
    public AmountDetail recalculateAmount(String orderId) {
        return calculateAmount(orderId);
    }

    /**
     * 根据规则列表计算费用
     */
    private AmountDetail calculateByRules(int actualDurationSeconds, Integer presetDurationSeconds, Object rulesObj) {
        AmountDetail detail = new AmountDetail();
        detail.setActualDuration(actualDurationSeconds);

        // 转换为分钟
        int actualMinutes = actualDurationSeconds / 60;
        int presetMinutes = presetDurationSeconds != null ? presetDurationSeconds / 60 : 0;

        // 查找匹配的规则
        double totalPrice = 0.0;
        boolean useUnlimited = false;
        double unlimitedPrice = 0.0;
        int maxLimitedMinutes = 0;
        boolean foundPresetRule = false;

        // 解析规则列表
        for (Object ruleObj : (Iterable) rulesObj) {
            JSONObject rule = JSONUtil.parseObj(ruleObj);
            Boolean unlimited = rule.getBool("unlimited", false);
            Integer minutes = rule.getInt("minutes");
            Double price = rule.getDouble("price");

            if (Boolean.TRUE.equals(unlimited)) {
                unlimitedPrice = price != null ? price : 0.0;
                continue;
            }

            // 记录最大有限时长
            if (minutes != null && minutes > maxLimitedMinutes) {
                maxLimitedMinutes = minutes;
            }

            // 优先匹配预设时长的规则
            if (minutes != null && presetMinutes > 0 && minutes == presetMinutes) {
                totalPrice = price != null ? price : 0.0;
                foundPresetRule = true;
                break;
            }
        }

        // 如果找到了预设时长的规则，直接使用
        if (foundPresetRule) {
            detail.setTotalAmount(roundAmount(totalPrice));
            detail.setNormalAmount(roundAmount(totalPrice));
            detail.setOvertimeAmount(0.0);
            detail.setBillingType("preset");
            detail.setUnitPrice(totalPrice);

            log.info("使用预设时长规则计费: presetMinutes={}分钟, price={}元", presetMinutes, totalPrice);
            return detail;
        }

        // 如果没有预设时长或者没有找到匹配的预设规则，按实际时长计算
        for (Object ruleObj : (Iterable) rulesObj) {
            JSONObject rule = JSONUtil.parseObj(ruleObj);
            Boolean unlimited = rule.getBool("unlimited", false);
            Integer minutes = rule.getInt("minutes");
            Double price = rule.getDouble("price");

            if (Boolean.TRUE.equals(unlimited)) {
                continue;
            }

            // 如果实际时长小于等于规则时长，使用该规则的价格
            if (minutes != null && actualMinutes <= minutes) {
                totalPrice = price != null ? price : 0.0;
                break;
            }

            // 如果实际时长大于规则时长，更新为当前规则价格（后续规则可能更合适）
            if (minutes != null && actualMinutes > minutes) {
                totalPrice = price != null ? price : 0.0;
            }
        }

        // 如果有不限时规则且实际时长超过了所有有限规则，使用不限时价格
        if (unlimitedPrice > 0 && actualMinutes > maxLimitedMinutes) {
            useUnlimited = true;
            totalPrice = unlimitedPrice;
        }

        detail.setTotalAmount(roundAmount(totalPrice));
        detail.setNormalAmount(roundAmount(totalPrice));
        detail.setOvertimeAmount(0.0);
        detail.setBillingType(useUnlimited ? "unlimited" : "tiered");
        detail.setUnitPrice(totalPrice);

        log.info("按实际时长计费: actualMinutes={}分钟, totalPrice={}元", actualMinutes, totalPrice);

        return detail;
    }

    /**
     * 使用默认规则计算费用
     */
    private AmountDetail calculateByDefaultRule(int actualDurationSeconds, Integer presetDurationSeconds) {
        AmountDetail detail = new AmountDetail();
        detail.setActualDuration(actualDurationSeconds);

        // 默认按每小时 30 元计算
        double hours = Math.ceil((double) actualDurationSeconds / 3600);
        double amount = hours * 30.0;

        detail.setTotalAmount(roundAmount(amount));
        detail.setNormalAmount(roundAmount(amount));
        detail.setOvertimeAmount(0.0);
        detail.setBillingType("hour");
        detail.setUnitPrice(30.0);

        return detail;
    }

    /**
     * 金额保留两位小数
     */
    private double roundAmount(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    /**
     * 获取计费规则配置
     */
    private String getBillingRuleConfig() {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getConfigKey, BILLING_RULE_KEY);
        Config config = configMapper.selectOne(wrapper);

        if (config == null) {
            log.warn("计费规则配置不存在");
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.CONFIG_NOT_FOUND, "计费规则配置不存在，请先在设置页面配置计费规则");
        }

        return config.getConfigValue();
    }

    /**
     * 获取默认计费规则
     *
     * @return 默认计费规则
     */
    private com.pindou.timer.dto.BillingRule getDefaultBillingRule() {
        com.pindou.timer.dto.BillingRule rule = new com.pindou.timer.dto.BillingRule();
        rule.setType("hour");
        rule.setPricePerHour(30.0);
        rule.setPricePerMinute(0.5);
        rule.setOvertimeRate(DEFAULT_OVERTIME_RATE);
        return rule;
    }
}

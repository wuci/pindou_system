package com.pindou.timer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pindou.timer.entity.SysConfig;
import com.pindou.timer.mapper.SysConfigMapper;
import com.pindou.timer.service.BillingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计费服务实现类
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Service
public class BillingServiceImpl implements BillingService {

    private final SysConfigMapper sysConfigMapper;
    private final ObjectMapper objectMapper;

    // 默认计费配置（当数据库中没有配置时使用）
    private static final BigDecimal DEFAULT_HOURLY_RATE = new BigDecimal("30");  // 每小时30元
    private static final BigDecimal DEFAULT_OVERTIME_RATE = new BigDecimal("1.5"); // 超时费率1.5倍
    private static final String DEFAULT_BILLING_MODE = "hour";  // 计费模式：hour-按小时 minute-按分钟

    public BillingServiceImpl(SysConfigMapper sysConfigMapper, ObjectMapper objectMapper) {
        this.sysConfigMapper = sysConfigMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public BillingResult calculateBilling(Long duration, Long pauseDuration, Integer presetDuration) {
        log.info("计算费用: duration={}, pauseDuration={}, presetDuration={}", duration, pauseDuration, presetDuration);

        // 获取计费配置
        BillingConfig config = getBillingConfig();

        // 计算实际使用时长（总时长 - 暂停时长）
        long actualDuration = duration - pauseDuration;

        // 计算正常时长和超时时长
        long normalDuration = actualDuration;
        long overtimeDuration = 0;

        if (presetDuration != null && actualDuration > presetDuration) {
            normalDuration = presetDuration;
            overtimeDuration = actualDuration - presetDuration;
        }

        // 计算正常费用
        BigDecimal normalAmount = calculateFee(normalDuration, config.getHourlyRate(), config.getBillingMode());

        // 计算超时费用（应用超时费率）
        BigDecimal overtimeAmount = BigDecimal.ZERO;
        if (overtimeDuration > 0) {
            BigDecimal overtimeHourlyRate = config.getHourlyRate().multiply(config.getOvertimeRate());
            overtimeAmount = calculateFee(overtimeDuration, overtimeHourlyRate, config.getBillingMode());
        }

        // 计算总费用
        BigDecimal totalAmount = normalAmount.add(overtimeAmount);

        // 构建金额明细JSON
        String amountDetailJson = buildAmountDetailJson(normalAmount, overtimeAmount, totalAmount);

        log.info("费用计算完成: normalAmount={}, overtimeAmount={}, totalAmount={}",
                normalAmount, overtimeAmount, totalAmount);

        return new BillingResult(normalAmount, overtimeAmount, totalAmount, amountDetailJson);
    }

    /**
     * 根据时长和费率计算费用
     *
     * @param duration   时长（秒）
     * @param hourlyRate 小时费率
     * @param billingMode 计费模式
     * @return 费用
     */
    private BigDecimal calculateFee(long duration, BigDecimal hourlyRate, String billingMode) {
        if ("hour".equals(billingMode)) {
            // 按小时计费（不足一小时按一小时算）
            long hours = (duration + 3599) / 3600;  // 向上取整
            return hourlyRate.multiply(new BigDecimal(hours));
        } else if ("minute".equals(billingMode)) {
            // 按分钟计费
            long minutes = (duration + 59) / 60;  // 向上取整
            BigDecimal minuteRate = hourlyRate.divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);
            return minuteRate.multiply(new BigDecimal(minutes));
        } else {
            // 默认按小时计费
            long hours = (duration + 3599) / 3600;
            return hourlyRate.multiply(new BigDecimal(hours));
        }
    }

    /**
     * 获取计费配置
     */
    private BillingConfig getBillingConfig() {
        try {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysConfig::getConfigKey, "billing");
            SysConfig configEntity = sysConfigMapper.selectOne(wrapper);

            if (configEntity != null) {
                return objectMapper.readValue(configEntity.getConfigValue(), BillingConfig.class);
            }
        } catch (Exception e) {
            log.warn("读取计费配置失败，使用默认配置: {}", e.getMessage());
        }

        // 返回默认配置
        BillingConfig defaultConfig = new BillingConfig();
        defaultConfig.setBillingMode(DEFAULT_BILLING_MODE);
        defaultConfig.setHourlyRate(DEFAULT_HOURLY_RATE);
        defaultConfig.setOvertimeRate(DEFAULT_OVERTIME_RATE);
        return defaultConfig;
    }

    /**
     * 构建金额明细JSON
     */
    private String buildAmountDetailJson(BigDecimal normalAmount, BigDecimal overtimeAmount, BigDecimal totalAmount) {
        try {
            AmountDetail detail = new AmountDetail();
            detail.setNormalFee(normalAmount);
            detail.setOvertimeFee(overtimeAmount);
            detail.setTotalFee(totalAmount);
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException e) {
            log.error("构建金额明细JSON失败", e);
            return "{\"normalFee\":0,\"overtimeFee\":0,\"totalFee\":0}";
        }
    }

    /**
     * 计费配置
     */
    public static class BillingConfig {
        private String billingMode;      // 计费模式：hour-按小时 minute-按分钟
        private BigDecimal hourlyRate;   // 小时费率
        private BigDecimal overtimeRate; // 超时费率

        public String getBillingMode() {
            return billingMode;
        }

        public void setBillingMode(String billingMode) {
            this.billingMode = billingMode;
        }

        public BigDecimal getHourlyRate() {
            return hourlyRate;
        }

        public void setHourlyRate(BigDecimal hourlyRate) {
            this.hourlyRate = hourlyRate;
        }

        public BigDecimal getOvertimeRate() {
            return overtimeRate;
        }

        public void setOvertimeRate(BigDecimal overtimeRate) {
            this.overtimeRate = overtimeRate;
        }
    }

    /**
     * 金额明细
     */
    public static class AmountDetail {
        private BigDecimal normalFee;
        private BigDecimal overtimeFee;
        private BigDecimal totalFee;

        public BigDecimal getNormalFee() {
            return normalFee;
        }

        public void setNormalFee(BigDecimal normalFee) {
            this.normalFee = normalFee;
        }

        public BigDecimal getOvertimeFee() {
            return overtimeFee;
        }

        public void setOvertimeFee(BigDecimal overtimeFee) {
            this.overtimeFee = overtimeFee;
        }

        public BigDecimal getTotalFee() {
            return totalFee;
        }

        public void setTotalFee(BigDecimal totalFee) {
            this.totalFee = totalFee;
        }
    }
}

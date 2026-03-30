package com.pindou.timer.service;

import java.math.BigDecimal;

/**
 * 计费服务接口
 *
 * @author pindou
 * @since 1.0.0
 */
public interface BillingService {

    /**
     * 计算订单费用
     *
     * @param duration        实际使用时长（秒）
     * @param pauseDuration   暂停时长（秒）
     * @param presetDuration  预设时长（秒），null表示不设时长
     * @return 费用详情
     */
    BillingResult calculateBilling(Long duration, Long pauseDuration, Integer presetDuration);

    /**
     * 计费结果
     */
    class BillingResult {
        private final BigDecimal normalAmount;    // 正常费用
        private final BigDecimal overtimeAmount;  // 超时费用
        private final BigDecimal totalAmount;     // 总费用
        private final String amountDetailJson;    // 金额明细JSON

        public BillingResult(BigDecimal normalAmount, BigDecimal overtimeAmount,
                           BigDecimal totalAmount, String amountDetailJson) {
            this.normalAmount = normalAmount;
            this.overtimeAmount = overtimeAmount;
            this.totalAmount = totalAmount;
            this.amountDetailJson = amountDetailJson;
        }

        public BigDecimal getNormalAmount() {
            return normalAmount;
        }

        public BigDecimal getOvertimeAmount() {
            return overtimeAmount;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public String getAmountDetailJson() {
            return amountDetailJson;
        }
    }
}

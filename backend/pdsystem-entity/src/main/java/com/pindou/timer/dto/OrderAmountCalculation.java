package com.pindou.timer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单金额计算结果值对象
 *
 * @author wuci
 * @date 2026-04-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAmountCalculation {

    /** 原价 */
    private double originalAmount;

    /** 折后价 */
    private double finalAmount;

    /** 费用明细 */
    private AmountDetail amountDetail;

    /**
     * 创建金额计算结果
     *
     * @param originalAmount 原价
     * @param finalAmount 折后价
     * @param amountDetail 费用明细
     * @return 金额计算结果
     */
    public static OrderAmountCalculation of(double originalAmount, double finalAmount, AmountDetail amountDetail) {
        return new OrderAmountCalculation(originalAmount, finalAmount, amountDetail);
    }

    /**
     * 获取折扣金额
     */
    public double getDiscountAmount() {
        return originalAmount - finalAmount;
    }

    /**
     * 获取折扣率
     */
    public BigDecimal getDiscountRate() {
        if (originalAmount == 0) {
            return BigDecimal.ONE;
        }
        return BigDecimal.valueOf(finalAmount).divide(BigDecimal.valueOf(originalAmount), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 是否应用了折扣
     */
    public boolean hasDiscount() {
        return Math.abs(originalAmount - finalAmount) > 0.01;
    }

    /**
     * 获取原价（BigDecimal）
     */
    public BigDecimal getOriginalAmountAsDecimal() {
        return BigDecimal.valueOf(originalAmount);
    }

    /**
     * 获取折后价（BigDecimal）
     */
    public BigDecimal getFinalAmountAsDecimal() {
        return BigDecimal.valueOf(finalAmount);
    }
}

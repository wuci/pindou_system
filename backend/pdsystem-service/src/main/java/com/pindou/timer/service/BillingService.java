package com.pindou.timer.service;

import com.pindou.timer.dto.AmountDetail;

/**
 * 计费服务接口
 *
 * @author wuci
 * @date 2026-03-28
 */
public interface BillingService {

    /**
     * 获取计费规则
     *
     * @return 计费规则
     */
    com.pindou.timer.dto.BillingRule getBillingRule();

    /**
     * 计算订单金额
     *
     * @param actualDuration 计费时长（秒）
     * @param presetDuration 预设时长（秒），null表示不设时长
     * @return 金额明细
     */
    AmountDetail calculateAmount(Integer actualDuration, Integer presetDuration);

    /**
     * 重新计算订单金额（用于订单更新时）
     *
     * @param orderId 订单ID
     * @return 金额明细
     */
    AmountDetail recalculateAmount(String orderId);
}

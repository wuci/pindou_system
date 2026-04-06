package com.pindou.timer.service;

import com.pindou.timer.dto.CalculateDiscountRequest;
import com.pindou.timer.dto.CalculateDiscountResponse;

/**
 * 会员折扣服务接口
 *
 * @author wuci
 * @date 2026-04-06
 */
public interface MemberDiscountService {

    /**
     * 计算会员折扣
     *
     * @param memberId 会员ID
     * @param amount 订单金额
     * @return 折扣计算结果
     */
    CalculateDiscountResponse calculateDiscount(Long memberId, Double amount);

    /**
     * 计算会员折扣（使用请求对象）
     *
     * @param memberId 会员ID
     * @param request 折扣计算请求
     * @return 折扣计算结果
     */
    CalculateDiscountResponse calculateDiscount(Long memberId, CalculateDiscountRequest request);

    /**
     * 判断订单是否需要应用会员折扣
     *
     * @param orderOriginalAmount 订单原价
     * @param orderFinalAmount 订单折后价
     * @return 是否需要应用折扣
     */
    boolean needApplyDiscount(Double orderOriginalAmount, Double orderFinalAmount);

    /**
     * 应用会员折扣到金额计算结果
     *
     * @param memberId 会员ID
     * @param originalAmount 原价
     * @return 折后价
     */
    Double applyDiscount(Long memberId, Double originalAmount);
}

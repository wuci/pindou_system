package com.pindou.timer.service.impl;

import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.dto.CalculateDiscountRequest;
import com.pindou.timer.dto.CalculateDiscountResponse;
import com.pindou.timer.entity.Member;
import com.pindou.timer.entity.MemberLevel;
import com.pindou.timer.mapper.MemberLevelMapper;
import com.pindou.timer.mapper.MemberMapper;
import com.pindou.timer.service.MemberDiscountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 会员折扣服务实现类
 *
 * @author wuci
 * @date 2026-04-06
 */
@Slf4j
@Service
public class MemberDiscountServiceImpl implements MemberDiscountService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Override
    public CalculateDiscountResponse calculateDiscount(Long memberId, Double amount) {
        CalculateDiscountRequest request = new CalculateDiscountRequest();
        request.setAmount(BigDecimal.valueOf(amount));
        return calculateDiscount(memberId, request);
    }

    @Override
    public CalculateDiscountResponse calculateDiscount(Long memberId, CalculateDiscountRequest request) {
        log.info("计算会员折扣: memberId={}, amount={}", memberId, request.getAmount());

        // 查询会员
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 查询会员等级
        MemberLevel level = memberLevelMapper.selectById(member.getLevelId());
        if (level == null) {
            throw new BusinessException(ErrorCode.MEMBER_LEVEL_NOT_FOUND);
        }

        // 计算折扣
        BigDecimal originalAmount = request.getAmount();
        BigDecimal discountRate = level.getDiscountRate();
        BigDecimal discountAmount = originalAmount.multiply(BigDecimal.ONE.subtract(discountRate))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = originalAmount.subtract(discountAmount);

        CalculateDiscountResponse response = new CalculateDiscountResponse();
        response.setOriginalAmount(originalAmount);
        response.setDiscountRate(discountRate);
        response.setDiscountAmount(discountAmount);
        response.setFinalAmount(finalAmount);

        log.info("计算会员折扣成功: originalAmount={}, discountRate={}, discountAmount={}, finalAmount={}",
                originalAmount, discountRate, discountAmount, finalAmount);

        return response;
    }

    @Override
    public boolean needApplyDiscount(Double orderOriginalAmount, Double orderFinalAmount) {
        // 订单无原价，需要应用会员折扣
        if (orderOriginalAmount == null || orderOriginalAmount == 0) {
            return true;
        }
        // 订单未应用折扣（原价等于折后价），需要应用会员折扣
        if (Math.abs(orderOriginalAmount - orderFinalAmount) < 0.01) {
            return true;
        }
        // 订单已应用折扣，跳过
        return false;
    }

    @Override
    public Double applyDiscount(Long memberId, Double originalAmount) {
        if (memberId == null || originalAmount == null) {
            return originalAmount;
        }

        try {
            CalculateDiscountRequest request = new CalculateDiscountRequest();
            request.setAmount(BigDecimal.valueOf(originalAmount));
            CalculateDiscountResponse response = calculateDiscount(memberId, request);

            log.info("会员折扣已应用: originalAmount={}, finalAmount={}",
                    originalAmount, response.getFinalAmount());

            return response.getFinalAmount().doubleValue();
        } catch (Exception e) {
            log.warn("计算会员折扣失败，使用原价: memberId={}, error={}", memberId, e.getMessage());
            return originalAmount;
        }
    }
}

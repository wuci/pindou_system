package com.pindou.timer.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Discount;
import com.pindou.timer.entity.MemberLevel;
import com.pindou.timer.entity.Member;
import com.pindou.timer.mapper.DiscountMapper;
import com.pindou.timer.mapper.MemberLevelMapper;
import com.pindou.timer.mapper.MemberMapper;
import com.pindou.timer.service.DiscountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 折扣Service实现类
 *
 * @author wuci
 * @date 2026-04-06
 */
@Slf4j
@Service
public class DiscountServiceImpl extends ServiceImpl<DiscountMapper, Discount> implements DiscountService {

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Autowired
    private MemberMapper memberMapper;

    /**
     * 折扣类型枚举
     */
    private static final class DiscountType {
        /** 固定折扣 */
        public static final int FIXED = 1;
        /** 会员折扣 */
        public static final int MEMBER = 2;
        /** 活动折扣 */
        public static final int ACTIVITY = 3;
    }

    @Override
    public PageResult<DiscountResponse> getDiscountList(DiscountQueryRequest request) {
        log.info("获取折扣列表: request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<Discount> wrapper = new LambdaQueryWrapper<>();

        // 折扣名称模糊查询
        if (StringUtils.isNotBlank(request.getName())) {
            wrapper.like(Discount::getName, request.getName());
        }

        // 折扣类型筛选
        if (request.getType() != null) {
            wrapper.eq(Discount::getType, request.getType());
        }

        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(Discount::getStatus, request.getStatus());
        }

        // 按排序和创建时间排序
        wrapper.orderByAsc(Discount::getSort)
                .orderByDesc(Discount::getCreatedAt);

        // 分页查询
        Page<Discount> page = new Page<>(request.getPage(), request.getPageSize());
        Page<Discount> resultPage = page(page);

        // 转换结果
        List<DiscountResponse> records = resultPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageResult<DiscountResponse> pageResult = new PageResult<>();
        pageResult.setList(records);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage(request.getPage());
        pageResult.setPageSize(request.getPageSize());

        log.info("获取折扣列表成功: total={}", pageResult.getTotal());
        return pageResult;
    }

    @Override
    public List<DiscountResponse> getAllActiveDiscounts() {
        log.info("获取所有启用的折扣列表");

        long currentTime = System.currentTimeMillis();

        LambdaQueryWrapper<Discount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Discount::getStatus, 1)
                .and(w -> w.isNull(Discount::getStartTime)
                        .or().le(Discount::getStartTime, currentTime))
                .and(w -> w.isNull(Discount::getEndTime)
                        .or().ge(Discount::getEndTime, currentTime))
                .orderByAsc(Discount::getSort)
                .orderByDesc(Discount::getCreatedAt);

        List<Discount> discounts = list(wrapper);

        return discounts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDiscount(CreateDiscountRequest request) {
        log.info("创建折扣: name={}", request.getName());

        // 验证会员折扣类型必须指定会员等级
        if (request.getType() != null && request.getType().intValue() == DiscountType.MEMBER && request.getMemberLevelId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会员折扣类型必须指定会员等级");
        }

        // 验证时间范围
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // 创建折扣
        Discount discount = new Discount();
        discount.setId(IdUtil.simpleUUID());
        discount.setName(request.getName());
        discount.setType(request.getType());
        discount.setDiscountRate(request.getDiscountRate());
        discount.setMinAmount(request.getMinAmount());
        discount.setMaxDiscount(request.getMaxDiscount());
        discount.setMemberLevelId(request.getMemberLevelId());
        discount.setStartTime(request.getStartTime());
        discount.setEndTime(request.getEndTime());
        discount.setStatus(request.getStatus());
        discount.setSort(request.getSort() != null ? request.getSort() : 0);
        discount.setDescription(request.getDescription());
        discount.setCreatedAt(System.currentTimeMillis());
        discount.setUpdatedAt(System.currentTimeMillis());

        save(discount);

        log.info("创建折扣成功: discountId={}, name={}", discount.getId(), discount.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDiscount(String discountId, UpdateDiscountRequest request) {
        log.info("更新折扣: discountId={}", discountId);

        // 查询折扣
        Discount discount = getById(discountId);
        if (discount == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "折扣不存在");
        }

        // 验证会员折扣类型必须指定会员等级
        if (request.getType() != null && request.getType().intValue() == DiscountType.MEMBER && request.getMemberLevelId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会员折扣类型必须指定会员等级");
        }

        // 验证时间范围
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // 更新字段
        discount.setName(request.getName());
        discount.setType(request.getType());
        discount.setDiscountRate(request.getDiscountRate());
        discount.setMinAmount(request.getMinAmount());
        discount.setMaxDiscount(request.getMaxDiscount());
        discount.setMemberLevelId(request.getMemberLevelId());
        discount.setStartTime(request.getStartTime());
        discount.setEndTime(request.getEndTime());
        discount.setStatus(request.getStatus());
        discount.setSort(request.getSort());
        discount.setDescription(request.getDescription());
        discount.setUpdatedAt(System.currentTimeMillis());

        updateById(discount);

        log.info("更新折扣成功: discountId={}", discountId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscount(String discountId) {
        log.info("删除折扣: discountId={}", discountId);

        // 查询折扣
        Discount discount = getById(discountId);
        if (discount == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "折扣不存在");
        }

        // 先更新 updated_at，再删除
        discount.setUpdatedAt(System.currentTimeMillis());
        updateById(discount);
        removeById(discount);
        log.info("删除折扣成功: discountId={}", discountId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDiscountStatus(String discountId, Integer status) {
        log.info("更新折扣状态: discountId={}, status={}", discountId, status);

        // 查询折扣
        Discount discount = getById(discountId);
        if (discount == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "折扣不存在");
        }

        // 更新状态
        discount.setStatus(status);
        discount.setUpdatedAt(System.currentTimeMillis());

        updateById(discount);

        log.info("更新折扣状态成功: discountId={}, status={}", discountId, status);
    }

    @Override
    public CalculateDiscountResponse calculateDiscount(CalculateDiscountRequest request) {
        log.info("计算订单折扣: request={}", request);

        long currentTime = System.currentTimeMillis();
        BigDecimal finalDiscount = BigDecimal.ZERO;
        BigDecimal finalRate = BigDecimal.ONE;
        String appliedDiscountName = "";

        // 查询适用的折扣
        LambdaQueryWrapper<Discount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Discount::getStatus, 1)
                .and(w -> w.isNull(Discount::getStartTime)
                        .or().le(Discount::getStartTime, currentTime))
                .and(w -> w.isNull(Discount::getEndTime)
                        .or().ge(Discount::getEndTime, currentTime))
                .orderByAsc(Discount::getSort)
                .orderByDesc(Discount::getCreatedAt);

        List<Discount> discounts = list(wrapper);

        // 遍历折扣，找到最优惠的
        for (Discount discount : discounts) {
            // 检查最低消费金额
            if (discount.getMinAmount() != null && request.getAmount().compareTo(discount.getMinAmount()) < 0) {
                continue;
            }

            // 检查会员折扣
            if (discount.getType() != null && discount.getType().intValue() == DiscountType.MEMBER) {
                if (request.getMemberLevelId() == null) {
                    continue;
                }
                if (!request.getMemberLevelId().equals(discount.getMemberLevelId())) {
                    continue;
                }
            }

            // 计算折扣金额
            BigDecimal discountAmount = request.getAmount()
                    .multiply(BigDecimal.ONE.subtract(discount.getDiscountRate()))
                    .setScale(2, RoundingMode.HALF_UP);

            // 检查最高优惠金额限制
            if (discount.getMaxDiscount() != null && discountAmount.compareTo(discount.getMaxDiscount()) > 0) {
                discountAmount = discount.getMaxDiscount();
            }

            // 选择更优惠的折扣
            if (discountAmount.compareTo(finalDiscount) > 0) {
                finalDiscount = discountAmount;
                finalRate = discount.getDiscountRate();
                appliedDiscountName = discount.getName();
            }
        }

        // 计算折后金额
        BigDecimal finalAmount = request.getAmount().subtract(finalDiscount);

        CalculateDiscountResponse response = new CalculateDiscountResponse();
        response.setOriginalAmount(request.getAmount());
        response.setDiscountRate(finalRate);
        response.setDiscountAmount(finalDiscount);
        response.setFinalAmount(finalAmount);
        response.setAppliedDiscountName(appliedDiscountName);

        log.info("计算折扣成功: originalAmount={}, discountAmount={}, finalAmount={}",
                request.getAmount(), finalDiscount, finalAmount);

        return response;
    }

    @Override
    public CalculateDiscountResponse calculateDiscountById(String discountId, BigDecimal amount, Long memberId) {
        log.info("根据折扣ID计算折扣: discountId={}, amount={}, memberId={}", discountId, amount, memberId);

        // 查询折扣
        Discount discount = getById(discountId);
        if (discount == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "折扣不存在");
        }

        // 检查折扣状态
        if (discount.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "折扣未启用");
        }

        // 检查有效期
        long currentTime = System.currentTimeMillis();
        if (discount.getStartTime() != null && discount.getStartTime() > currentTime) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "折扣未开始");
        }
        if (discount.getEndTime() != null && discount.getEndTime() < currentTime) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "折扣已结束");
        }

        // 检查最低消费金额
        if (discount.getMinAmount() != null && amount.compareTo(discount.getMinAmount()) < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    String.format("未达到最低消费金额¥%s", discount.getMinAmount()));
        }

        // 检查会员折扣
        if (discount.getType() != null && discount.getType().intValue() == DiscountType.MEMBER) {
            if (memberId == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "该折扣仅限会员使用");
            }
            // 获取会员等级
            Member member = memberMapper.selectById(memberId);
            if (member == null) {
                throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
            }
            if (!member.getLevelId().equals(discount.getMemberLevelId())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "会员等级不匹配");
            }
        }

        // 计算折扣金额
        BigDecimal discountAmount = amount
                .multiply(BigDecimal.ONE.subtract(discount.getDiscountRate()))
                .setScale(2, RoundingMode.HALF_UP);

        // 检查最高优惠金额限制
        if (discount.getMaxDiscount() != null && discountAmount.compareTo(discount.getMaxDiscount()) > 0) {
            discountAmount = discount.getMaxDiscount();
        }

        // 计算折后金额
        BigDecimal finalAmount = amount.subtract(discountAmount);

        CalculateDiscountResponse response = new CalculateDiscountResponse();
        response.setOriginalAmount(amount);
        response.setDiscountRate(discount.getDiscountRate());
        response.setDiscountAmount(discountAmount);
        response.setFinalAmount(finalAmount);
        response.setAppliedDiscountName(discount.getName());

        log.info("根据折扣ID计算折扣成功: originalAmount={}, discountRate={}, discountAmount={}, finalAmount={}, discountName={}",
                amount, discount.getDiscountRate(), discountAmount, finalAmount, discount.getName());

        return response;
    }

    /**
     * 验证时间范围
     */
    private void validateTimeRange(Long startTime, Long endTime) {
        if (startTime != null && endTime != null && startTime >= endTime) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "开始时间不能晚于或等于结束时间");
        }
    }

    /**
     * 转换为响应DTO
     */
    private DiscountResponse convertToResponse(Discount discount) {
        DiscountResponse response = new DiscountResponse();
        response.setId(discount.getId());
        response.setName(discount.getName());
        response.setType(discount.getType());
        response.setTypeName(getTypeName(discount.getType()));
        response.setDiscountRate(discount.getDiscountRate());
        response.setMinAmount(discount.getMinAmount());
        response.setMaxDiscount(discount.getMaxDiscount());
        response.setMemberLevelId(discount.getMemberLevelId());
        response.setMemberLevelName(getMemberLevelName(discount.getMemberLevelId()));
        response.setStartTime(discount.getStartTime());
        response.setEndTime(discount.getEndTime());
        response.setStatus(discount.getStatus());
        response.setSort(discount.getSort());
        response.setDescription(discount.getDescription());
        response.setCreatedAt(discount.getCreatedAt());
        response.setUpdatedAt(discount.getUpdatedAt());
        return response;
    }

    /**
     * 获取折扣类型名称
     */
    private String getTypeName(Integer type) {
        if (type == null) {
            return "";
        }
        int typeValue = type.intValue();
        switch (typeValue) {
            case DiscountType.FIXED:
                return "固定折扣";
            case DiscountType.MEMBER:
                return "会员折扣";
            case DiscountType.ACTIVITY:
                return "活动折扣";
            default:
                return "未知";
        }
    }

    /**
     * 获取会员等级名称
     */
    private String getMemberLevelName(Long memberLevelId) {
        if (memberLevelId == null) {
            return null;
        }
        MemberLevel level = memberLevelMapper.selectById(memberLevelId);
        return level != null ? level.getName() : null;
    }
}

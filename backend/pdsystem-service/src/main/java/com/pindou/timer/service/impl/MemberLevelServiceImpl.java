package com.pindou.timer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Member;
import com.pindou.timer.entity.MemberLevel;
import com.pindou.timer.mapper.MemberLevelMapper;
import com.pindou.timer.mapper.MemberMapper;
import com.pindou.timer.service.MemberLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员等级Service实现类
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Service
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelMapper, MemberLevel> implements MemberLevelService {

    private final MemberMapper memberMapper;

    public MemberLevelServiceImpl(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public List<MemberLevelResponse> getAllLevels() {
        log.info("获取所有会员等级");

        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MemberLevel::getSort);

        List<MemberLevel> levels = list(wrapper);

        return levels.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLevel(CreateMemberLevelRequest request) {
        log.info("创建会员等级: name={}", request.getName());

        // 验证名称唯一性
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getName, request.getName());
        if (count(wrapper) > 0) {
            throw new BusinessException(ErrorCode.MEMBER_LEVEL_NAME_EXISTS);
        }

        // 验证金额范围
        if (request.getMaxAmount() != null && request.getMinAmount().compareTo(request.getMaxAmount()) >= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "最小金额必须小于最大金额");
        }

        // 创建等级
        MemberLevel level = new MemberLevel();
        level.setName(request.getName());
        level.setMinAmount(request.getMinAmount());
        level.setMaxAmount(request.getMaxAmount());
        level.setDiscountRate(request.getDiscountRate());
        level.setSort(request.getSort());
        level.setCreatedAt(System.currentTimeMillis());
        level.setUpdatedAt(System.currentTimeMillis());

        save(level);

        log.info("创建会员等级成功: levelId={}, name={}", level.getId(), level.getName());
        return level.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLevel(Long levelId, UpdateMemberLevelRequest request) {
        log.info("更新会员等级: levelId={}", levelId);

        // 查询等级
        MemberLevel level = getById(levelId);
        if (level == null) {
            throw new BusinessException(ErrorCode.MEMBER_LEVEL_NOT_FOUND);
        }

        // 验证名称唯一性
        if (request.getName() != null && !request.getName().equals(level.getName())) {
            LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MemberLevel::getName, request.getName())
                    .ne(MemberLevel::getId, levelId);
            if (count(wrapper) > 0) {
                throw new BusinessException(ErrorCode.MEMBER_LEVEL_NAME_EXISTS);
            }
        }

        // 验证金额范围
        BigDecimal minAmount = request.getMinAmount() != null ? request.getMinAmount() : level.getMinAmount();
        BigDecimal maxAmount = request.getMaxAmount() != null ? request.getMaxAmount() : level.getMaxAmount();
        if (maxAmount != null && minAmount.compareTo(maxAmount) >= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "最小金额必须小于最大金额");
        }

        // 更新字段
        if (request.getName() != null) {
            level.setName(request.getName());
        }
        if (request.getMinAmount() != null) {
            level.setMinAmount(request.getMinAmount());
        }
        if (request.getMaxAmount() != null) {
            level.setMaxAmount(request.getMaxAmount());
        }
        if (request.getDiscountRate() != null) {
            level.setDiscountRate(request.getDiscountRate());
        }
        if (request.getSort() != null) {
            level.setSort(request.getSort());
        }

        level.setUpdatedAt(System.currentTimeMillis());
        updateById(level);

        log.info("更新会员等级成功: levelId={}", levelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLevel(Long levelId) {
        log.info("删除会员等级: levelId={}", levelId);

        // 查询等级
        MemberLevel level = getById(levelId);
        if (level == null) {
            throw new BusinessException(ErrorCode.MEMBER_LEVEL_NOT_FOUND);
        }

        // 检查是否有会员使用该等级
        LambdaQueryWrapper<Member> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(Member::getLevelId, levelId);
        if (memberMapper.selectCount(memberWrapper) > 0) {
            throw new BusinessException(ErrorCode.MEMBER_LEVEL_IN_USE);
        }

        // 删除等级
        removeById(levelId);

        log.info("删除会员等级成功: levelId={}", levelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int initDefaultLevels() {
        log.info("初始化默认会员等级");

        // 检查是否已存在等级
        long count = count();
        if (count > 0) {
            log.info("会员等级已存在，跳过初始化");
            return 0;
        }

        // 创建4个默认等级
        MemberLevel level1 = new MemberLevel();
        level1.setName("豆豆萌新");
        level1.setMinAmount(new BigDecimal("0.00"));
        level1.setMaxAmount(new BigDecimal("300.00"));
        level1.setDiscountRate(new BigDecimal("0.950"));
        level1.setSort(1);
        level1.setCreatedAt(System.currentTimeMillis());
        level1.setUpdatedAt(System.currentTimeMillis());

        MemberLevel level2 = new MemberLevel();
        level2.setName("熨烫能手");
        level2.setMinAmount(new BigDecimal("300.01"));
        level2.setMaxAmount(new BigDecimal("1000.00"));
        level2.setDiscountRate(new BigDecimal("0.900"));
        level2.setSort(2);
        level2.setCreatedAt(System.currentTimeMillis());
        level2.setUpdatedAt(System.currentTimeMillis());

        MemberLevel level3 = new MemberLevel();
        level3.setName("像素匠人");
        level3.setMinAmount(new BigDecimal("1000.01"));
        level3.setMaxAmount(new BigDecimal("3000.00"));
        level3.setDiscountRate(new BigDecimal("0.850"));
        level3.setSort(3);
        level3.setCreatedAt(System.currentTimeMillis());
        level3.setUpdatedAt(System.currentTimeMillis());

        MemberLevel level4 = new MemberLevel();
        level4.setName("熔豆典藏");
        level4.setMinAmount(new BigDecimal("3000.01"));
        level4.setMaxAmount(null);
        level4.setDiscountRate(new BigDecimal("0.800"));
        level4.setSort(4);
        level4.setCreatedAt(System.currentTimeMillis());
        level4.setUpdatedAt(System.currentTimeMillis());

        save(level1);
        save(level2);
        save(level3);
        save(level4);

        log.info("初始化默认会员等级成功，共创建4个等级");
        return 4;
    }

    @Override
    public MemberLevel findLevelByAmount(BigDecimal amount) {
        log.debug("根据累计消费查询等级: amount={}", amount);

        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(MemberLevel::getMinAmount, amount)
                .and(w -> w.isNull(MemberLevel::getMaxAmount)
                        .or()
                        .ge(MemberLevel::getMaxAmount, amount))
                .orderByDesc(MemberLevel::getSort)
                .last("LIMIT 1");

        return getOne(wrapper);
    }

    @Override
    public Long findLevelIdByAmount(BigDecimal amount) {
        MemberLevel level = findLevelByAmount(amount);
        return level != null ? level.getId() : null;
    }

    /**
     * 转换为响应DTO
     */
    private MemberLevelResponse convertToResponse(MemberLevel level) {
        MemberLevelResponse response = new MemberLevelResponse();
        response.setId(level.getId());
        response.setName(level.getName());
        response.setMinAmount(level.getMinAmount());
        response.setMaxAmount(level.getMaxAmount());
        response.setDiscountRate(level.getDiscountRate());
        response.setSort(level.getSort());
        response.setCreatedAt(level.getCreatedAt());
        response.setUpdatedAt(level.getUpdatedAt());
        return response;
    }
}

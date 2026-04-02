package com.pindou.timer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Member;
import com.pindou.timer.entity.MemberLevel;
import com.pindou.timer.entity.RechargeRecord;
import com.pindou.timer.mapper.MemberLevelMapper;
import com.pindou.timer.mapper.MemberMapper;
import com.pindou.timer.mapper.RechargeRecordMapper;
import com.pindou.timer.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员Service实现类
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    private final MemberLevelMapper memberLevelMapper;
    private final RechargeRecordMapper rechargeRecordMapper;

    public MemberServiceImpl(MemberLevelMapper memberLevelMapper, RechargeRecordMapper rechargeRecordMapper) {
        this.memberLevelMapper = memberLevelMapper;
        this.rechargeRecordMapper = rechargeRecordMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMember(CreateMemberRequest request) {
        log.info("创建会员: name={}, phone={}", request.getName(), request.getPhone());

        // 验证手机号唯一性
        if (!validatePhoneUnique(request.getPhone(), null)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_EXISTS);
        }

        // 查询最低等级（默认等级）
        Long defaultLevelId = findDefaultLevelId();
        if (defaultLevelId == null) {
            throw new BusinessException(ErrorCode.MEMBER_LEVEL_NOT_FOUND, "未配置默认会员等级，请先初始化会员等级");
        }

        // 创建会员
        Member member = new Member();
        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setAddress(request.getAddress());
        member.setTotalAmount(BigDecimal.ZERO);
        member.setLevelId(defaultLevelId);
        member.setCreatedAt(System.currentTimeMillis());
        member.setUpdatedAt(System.currentTimeMillis());

        save(member);

        log.info("创建会员成功: memberId={}, name={}, phone={}, levelId={}",
                member.getId(), member.getName(), member.getPhone(), member.getLevelId());
        return member.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMember(Long memberId, UpdateMemberRequest request) {
        log.info("更新会员: memberId={}", memberId);

        // 查询会员
        Member member = getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 更新字段
        member.setName(request.getName());
        if (request.getAddress() != null) {
            member.setAddress(request.getAddress());
        }
        member.setUpdatedAt(System.currentTimeMillis());

        updateById(member);

        log.info("更新会员成功: memberId={}", memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long memberId) {
        log.info("删除会员: memberId={}", memberId);

        // 查询会员
        Member member = getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 删除会员
        removeById(memberId);

        log.info("删除会员成功: memberId={}", memberId);
    }

    @Override
    public MemberResponse getMemberDetail(Long memberId) {
        log.info("获取会员详情: memberId={}", memberId);

        Member member = getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return convertToResponse(member);
    }

    @Override
    public PageResult<MemberResponse> getMemberList(MemberQueryRequest request) {
        log.info("获取会员列表: request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（手机号或姓名）
        if (StringUtils.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w.like(Member::getPhone, request.getKeyword())
                    .or()
                    .like(Member::getName, request.getKeyword()));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Member::getCreatedAt);

        // 分页查询
        Page<Member> page = new Page<>(request.getPage(), request.getPageSize());
        Page<Member> resultPage = page(page);

        // 转换结果
        List<MemberResponse> records = resultPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageResult<MemberResponse> pageResult = new PageResult<>();
        pageResult.setList(records);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage(request.getPage());
        pageResult.setPageSize(request.getPageSize());

        log.info("获取会员列表成功: total={}", pageResult.getTotal());
        return pageResult;
    }

    @Override
    public List<MemberResponse> searchMembers(String keyword) {
        log.info("搜索会员: keyword={}", keyword);

        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();

        // 如果关键词为空，返回最近创建的10个会员
        if (StringUtils.isBlank(keyword)) {
            wrapper.orderByDesc(Member::getCreatedAt)
                    .last("LIMIT 10");
        } else {
            // 否则根据手机号或姓名搜索
            wrapper.and(w -> w.like(Member::getPhone, keyword)
                            .or()
                            .like(Member::getName, keyword))
                    .orderByDesc(Member::getTotalAmount)
                    .last("LIMIT 10");
        }

        List<Member> members = list(wrapper);

        return members.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Member getByPhone(String phone) {
        return lambdaQuery()
                .eq(Member::getPhone, phone)
                .one();
    }

    @Override
    public boolean validatePhoneUnique(String phone, Long excludeId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getPhone, phone);
        if (excludeId != null) {
            wrapper.ne(Member::getId, excludeId);
        }
        return count(wrapper) == 0;
    }

    @Override
    public CalculateDiscountResponse calculateDiscount(Long memberId, CalculateDiscountRequest request) {
        log.info("计算会员折扣: memberId={}, originalAmount={}", memberId, request.getOriginalAmount());

        // 查询会员
        Member member = getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 查询会员等级
        MemberLevel level = memberLevelMapper.selectById(member.getLevelId());
        if (level == null) {
            throw new BusinessException(ErrorCode.MEMBER_LEVEL_NOT_FOUND);
        }

        // 计算折扣
        BigDecimal originalAmount = request.getOriginalAmount();
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
    public Long findLevelIdByAmount(BigDecimal amount) {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(MemberLevel::getMinAmount, amount)
                .and(w -> w.isNull(MemberLevel::getMaxAmount)
                        .or()
                        .ge(MemberLevel::getMaxAmount, amount))
                .orderByDesc(MemberLevel::getSort)
                .last("LIMIT 1");

        MemberLevel level = memberLevelMapper.selectOne(wrapper);
        return level != null ? level.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTotalAmount(Long memberId, BigDecimal amount) {
        log.info("更新会员累计消费: memberId={}, amount={}", memberId, amount);

        // 查询会员
        Member member = getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 更新累计消费
        BigDecimal newTotalAmount = member.getTotalAmount().add(amount);
        member.setTotalAmount(newTotalAmount);

        // 计算新等级
        Long newLevelId = findLevelIdByAmount(newTotalAmount);
        if (newLevelId != null && !newLevelId.equals(member.getLevelId())) {
            log.info("会员等级升级: memberId={}, oldLevelId={}, newLevelId={}, totalAmount={}",
                    memberId, member.getLevelId(), newLevelId, newTotalAmount);
            member.setLevelId(newLevelId);
        }

        member.setUpdatedAt(System.currentTimeMillis());
        updateById(member);

        log.info("更新会员累计消费成功: memberId={}, totalAmount={}, levelId={}",
                memberId, member.getTotalAmount(), member.getLevelId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal recharge(Long memberId, RechargeRequest request, String operatorId, String operatorName) {
        log.info("会员充值: memberId={}, amount={}, paymentMethod={}",
                memberId, request.getAmount(), request.getPaymentMethod());

        // 查询会员
        Member member = getById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 获取充值前余额
        BigDecimal balanceBefore = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
        BigDecimal rechargeAmount = request.getAmount();
        BigDecimal balanceAfter = balanceBefore.add(rechargeAmount);

        // 更新会员余额
        member.setBalance(balanceAfter);
        member.setUpdatedAt(System.currentTimeMillis());
        updateById(member);

        // 创建充值记录
        RechargeRecord record = new RechargeRecord();
        record.setMemberId(member.getId());
        record.setMemberName(member.getName());
        record.setMemberPhone(member.getPhone());
        record.setAmount(rechargeAmount);
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        record.setPaymentMethod(request.getPaymentMethod());
        record.setRemark(request.getRemark());
        record.setOperatorId(operatorId != null && !operatorId.isEmpty() ? Long.parseLong(operatorId) : null);
        record.setOperatorName(operatorName);
        record.setCreatedAt(System.currentTimeMillis());
        rechargeRecordMapper.insert(record);

        log.info("会员充值成功: memberId={}, rechargeAmount={}, balanceBefore={}, balanceAfter={}",
                member.getId(), rechargeAmount, balanceBefore, balanceAfter);

        return balanceAfter;
    }

    /**
     * 查询默认等级（最低等级）
     */
    private Long findDefaultLevelId() {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MemberLevel::getSort)
                .last("LIMIT 1");

        MemberLevel level = memberLevelMapper.selectOne(wrapper);
        return level != null ? level.getId() : null;
    }

    /**
     * 转换为响应DTO
     */
    private MemberResponse convertToResponse(Member member) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setName(member.getName());
        response.setPhone(member.getPhone());
        response.setAddress(member.getAddress());
        response.setTotalAmount(member.getTotalAmount() != null ? member.getTotalAmount() : BigDecimal.ZERO);
        response.setBalance(member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO);
        response.setLevelId(member.getLevelId());
        response.setCreatedAt(member.getCreatedAt());
        response.setUpdatedAt(member.getUpdatedAt());

        // 查询等级信息
        MemberLevel level = memberLevelMapper.selectById(member.getLevelId());
        if (level != null) {
            response.setLevelName(level.getName());
            response.setDiscountRate(level.getDiscountRate());
        }

        return response;
    }
}

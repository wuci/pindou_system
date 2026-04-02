package com.pindou.timer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Member;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员Service接口
 *
 * @author pindou
 * @since 1.0.0
 */
public interface MemberService extends IService<Member> {

    /**
     * 创建会员
     *
     * @param request 创建请求
     * @return 会员ID
     */
    Long createMember(CreateMemberRequest request);

    /**
     * 更新会员信息
     *
     * @param memberId 会员ID
     * @param request  更新请求
     */
    void updateMember(Long memberId, UpdateMemberRequest request);

    /**
     * 删除会员
     *
     * @param memberId 会员ID
     */
    void deleteMember(Long memberId);

    /**
     * 获取会员详情
     *
     * @param memberId 会员ID
     * @return 会员信息
     */
    MemberResponse getMemberDetail(Long memberId);

    /**
     * 获取会员列表（分页）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<MemberResponse> getMemberList(MemberQueryRequest request);

    /**
     * 搜索会员
     *
     * @param keyword 搜索关键词（手机号或姓名）
     * @return 会员列表
     */
    List<MemberResponse> searchMembers(String keyword);

    /**
     * 根据手机号查询会员
     *
     * @param phone 手机号
     * @return 会员
     */
    Member getByPhone(String phone);

    /**
     * 验证手机号唯一性
     *
     * @param phone 手机号
     * @param excludeId 排除的会员ID（更新时使用）
     * @return 是否唯一
     */
    boolean validatePhoneUnique(String phone, Long excludeId);

    /**
     * 计算会员折扣
     *
     * @param memberId 会员ID
     * @param request  折扣计算请求
     * @return 折扣计算结果
     */
    CalculateDiscountResponse calculateDiscount(Long memberId, CalculateDiscountRequest request);

    /**
     * 根据累计消费金额查询对应的会员等级
     *
     * @param amount 累计消费金额
     * @return 会员等级ID
     */
    Long findLevelIdByAmount(BigDecimal amount);

    /**
     * 更新会员累计消费
     *
     * @param memberId 会员ID
     * @param amount   消费金额（正数）
     */
    void updateTotalAmount(Long memberId, BigDecimal amount);

    /**
     * 会员充值
     *
     * @param memberId 会员ID
     * @param request 充值请求
     * @param operatorId 操作员ID
     * @param operatorName 操作员姓名
     * @return 充值后余额
     */
    BigDecimal recharge(Long memberId, RechargeRequest request, String operatorId, String operatorName);
}

package com.pindou.timer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.MemberLevel;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员等级Service接口
 *
 * @author pindou
 * @since 1.0.0
 */
public interface MemberLevelService extends IService<MemberLevel> {

    /**
     * 获取所有会员等级
     *
     * @return 会员等级列表
     */
    List<MemberLevelResponse> getAllLevels();

    /**
     * 创建会员等级
     *
     * @param request 创建请求
     * @return 等级ID
     */
    Long createLevel(CreateMemberLevelRequest request);

    /**
     * 更新会员等级
     *
     * @param levelId 会员等级ID
     * @param request 更新请求
     */
    void updateLevel(Long levelId, UpdateMemberLevelRequest request);

    /**
     * 删除会员等级
     *
     * @param levelId 会员等级ID
     */
    void deleteLevel(Long levelId);

    /**
     * 初始化默认会员等级
     *
     * @return 创建的等级数量
     */
    int initDefaultLevels();

    /**
     * 根据累计消费金额查询对应的会员等级
     *
     * @param amount 累计消费金额
     * @return 会员等级
     */
    MemberLevel findLevelByAmount(BigDecimal amount);

    /**
     * 根据累计消费金额查询对应的会员等级ID
     *
     * @param amount 累计消费金额
     * @return 会员等级ID，如果不存在返回null
     */
    Long findLevelIdByAmount(BigDecimal amount);
}

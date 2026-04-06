package com.pindou.timer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Discount;

import java.util.List;

/**
 * 折扣Service接口
 *
 * @author wuci
 * @date 2026-04-06
 */
public interface DiscountService extends IService<Discount> {

    /**
     * 获取折扣列表（分页）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<DiscountResponse> getDiscountList(DiscountQueryRequest request);

    /**
     * 获取所有启用的折扣列表
     *
     * @return 折扣列表
     */
    List<DiscountResponse> getAllActiveDiscounts();

    /**
     * 创建折扣
     *
     * @param request 创建请求
     */
    void createDiscount(CreateDiscountRequest request);

    /**
     * 更新折扣
     *
     * @param discountId 折扣ID
     * @param request    更新请求
     */
    void updateDiscount(String discountId, UpdateDiscountRequest request);

    /**
     * 删除折扣
     *
     * @param discountId 折扣ID
     */
    void deleteDiscount(String discountId);

    /**
     * 启用/禁用折扣
     *
     * @param discountId 折扣ID
     * @param status     状态：0-禁用，1-启用
     */
    void updateDiscountStatus(String discountId, Integer status);

    /**
     * 计算订单折扣
     *
     * @param request    计算折扣请求
     * @return 折扣响应
     */
    CalculateDiscountResponse calculateDiscount(CalculateDiscountRequest request);

    /**
     * 根据折扣ID计算折扣
     *
     * @param discountId 折扣ID
     * @param amount     订单金额
     * @param memberId   会员ID
     * @return 折扣响应
     */
    CalculateDiscountResponse calculateDiscountById(String discountId, java.math.BigDecimal amount, Long memberId);
}

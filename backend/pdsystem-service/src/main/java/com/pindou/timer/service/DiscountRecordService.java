package com.pindou.timer.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 折扣记录服务接口
 * 用于管理订单折扣记录的存储和计算
 *
 * @author wuci
 * @date 2026-04-06
 */
public interface DiscountRecordService {

    /**
     * 折扣记录常量
     */
    interface Constants {
        /** Redis Key前缀 */
        String REDIS_KEY_PREFIX = "order:discount:";

        /** 默认缓存过期时间（秒） - 24小时 */
        long DEFAULT_EXPIRE_SECONDS = 24 * 60 * 60;
    }

    /**
     * 添加折扣记录
     *
     * @param orderId 订单ID
     * @param discountId 折扣ID
     * @param discountRate 折扣率
     */
    void addDiscountRecord(String orderId, String discountId, BigDecimal discountRate);

    /**
     * 获取订单的所有折扣记录
     *
     * @param orderId 订单ID
     * @return 折扣率列表
     */
    List<BigDecimal> getDiscountRates(String orderId);

    /**
     * 获取订单的平均折扣率
     *
     * @param orderId 订单ID
     * @return 平均折扣率，如果没有折扣记录则返回null
     */
    BigDecimal getAverageDiscountRate(String orderId);

    /**
     * 清除订单的折扣记录
     * 用于订单完成后清理Redis数据
     *
     * @param orderId 订单ID
     */
    void clearDiscountRecords(String orderId);

    /**
     * 计算平均折扣率
     *
     * @param existingRates 已存在的折扣率列表
     * @param newDiscountRate 新的折扣率
     * @return 平均折扣率
     */
    BigDecimal calculateAverageRate(List<BigDecimal> existingRates, BigDecimal newDiscountRate);
}

package com.pindou.timer.service.impl;

import com.pindou.timer.service.DiscountRecordService;
import io.lettuce.core.internal.LettuceLists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 折扣记录服务实现类
 * 使用Redis存储订单的折扣记录，支持多次折扣的平均值计算
 *
 * @author wuci
 * @date 2026-04-06
 */
@Slf4j
@Service
public class DiscountRecordServiceImpl implements DiscountRecordService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addDiscountRecord(String orderId, String discountId, BigDecimal discountRate) {
        String key = buildRedisKey(orderId);
        try {
            redisTemplate.opsForList().rightPush(key, discountRate);
            // 设置过期时间，防止数据永久占用内存
            redisTemplate.expire(key, Constants.DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
            log.info("添加折扣记录成功: orderId={}, discountId={}, discountRate={}", orderId, discountId, discountRate);
        } catch (Exception e) {
            log.error("添加折扣记录失败: orderId={}, discountId={}, error={}", orderId, discountId, e.getMessage(), e);
            throw new RuntimeException("添加折扣记录失败", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BigDecimal> getDiscountRates(String orderId) {
        String key = buildRedisKey(orderId);
        try {
            List<Object> objects = redisTemplate.opsForList().range(key, 0, -1);
            if (objects == null || objects.isEmpty()) {
                return LettuceLists.newList();
            }
            return (List<BigDecimal>) (List<?>) objects;
        } catch (Exception e) {
            log.error("获取折扣记录失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return LettuceLists.newList();
        }
    }

    @Override
    public BigDecimal getAverageDiscountRate(String orderId) {
        List<BigDecimal> rates = getDiscountRates(orderId);
        if (rates.isEmpty()) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal rate : rates) {
            sum = sum.add(rate);
        }
        return sum.divide(BigDecimal.valueOf(rates.size()), 4, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public void clearDiscountRecords(String orderId) {
        String key = buildRedisKey(orderId);
        try {
            redisTemplate.delete(key);
            log.info("清除折扣记录成功: orderId={}", orderId);
        } catch (Exception e) {
            log.error("清除折扣记录失败: orderId={}, error={}", orderId, e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal calculateAverageRate(List<BigDecimal> existingRates, BigDecimal newDiscountRate) {
        if (existingRates == null || existingRates.isEmpty()) {
            return newDiscountRate;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal rate : existingRates) {
            sum = sum.add(rate);
        }
        // 加上新的折扣率
        sum = sum.add(newDiscountRate);
        // 计算平均值
        return sum.divide(BigDecimal.valueOf(existingRates.size() + 1), 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 构建Redis Key
     *
     * @param orderId 订单ID
     * @return Redis Key
     */
    private String buildRedisKey(String orderId) {
        return Constants.REDIS_KEY_PREFIX + orderId;
    }
}

package com.pindou.timer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pindou.timer.common.result.PageResult;

import java.util.Map;

/**
 * 消费记录Service接口
 *
 * @author pindou
 * @since 1.0.0
 */
public interface ConsumptionRecordService extends IService<com.pindou.timer.entity.ConsumptionRecord> {

    /**
     * 分页查询消费记录
     *
     * @param memberId 会员ID（可选）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 消费记录分页结果
     */
    PageResult<Map<String, Object>> getConsumptionRecords(Long memberId, Integer page, Integer pageSize);
}

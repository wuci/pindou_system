package com.pindou.timer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.RechargeRecordResponse;

import java.util.Map;

/**
 * 充值记录Service接口
 *
 * @author pindou
 * @since 1.0.0
 */
public interface RechargeRecordService extends IService<com.pindou.timer.entity.RechargeRecord> {

    /**
     * 分页查询充值记录
     *
     * @param memberId 会员ID（可选）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 充值记录分页结果
     */
    PageResult<RechargeRecordResponse> getRechargeRecords(Long memberId, Integer page, Integer pageSize);
}

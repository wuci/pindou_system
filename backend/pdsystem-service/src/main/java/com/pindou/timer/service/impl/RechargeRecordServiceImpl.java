package com.pindou.timer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.RechargeRecordResponse;
import com.pindou.timer.entity.RechargeRecord;
import com.pindou.timer.mapper.RechargeRecordMapper;
import com.pindou.timer.service.RechargeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 充值记录Service实现类
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Service
public class RechargeRecordServiceImpl extends ServiceImpl<RechargeRecordMapper, RechargeRecord> implements RechargeRecordService {

    @Override
    public PageResult<RechargeRecordResponse> getRechargeRecords(Long memberId, Integer page, Integer pageSize) {
        log.info("查询充值记录: memberId={}, page={}, pageSize={}", memberId, page, pageSize);

        LambdaQueryWrapper<RechargeRecord> wrapper = new LambdaQueryWrapper<>();
        if (memberId != null) {
            wrapper.eq(RechargeRecord::getMemberId, memberId);
        }
        wrapper.orderByDesc(RechargeRecord::getCreatedAt);

        Page<RechargeRecord> recordPage = new Page<>(page, pageSize);
        Page<RechargeRecord> resultPage = page(recordPage, wrapper);

        List<RechargeRecordResponse> records = resultPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageResult<RechargeRecordResponse> pageResult = new PageResult<>();
        pageResult.setList(records);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage(page);
        pageResult.setPageSize(pageSize);

        return pageResult;
    }

    /**
     * 转换为响应DTO
     */
    private RechargeRecordResponse convertToResponse(RechargeRecord record) {
        RechargeRecordResponse response = new RechargeRecordResponse();
        response.setId(record.getId());
        response.setMemberId(record.getMemberId());
        response.setMemberName(record.getMemberName());
        response.setMemberPhone(record.getMemberPhone());
        response.setAmount(record.getAmount());
        response.setBalanceBefore(record.getBalanceBefore());
        response.setBalanceAfter(record.getBalanceAfter());
        response.setPaymentMethod(record.getPaymentMethod());
        response.setPaymentMethodName(getPaymentMethodName(record.getPaymentMethod()));
        response.setRemark(record.getRemark());
        response.setOperatorName(record.getOperatorName());
        response.setCreatedAt(record.getCreatedAt());
        return response;
    }

    /**
     * 获取支付方式名称
     */
    private String getPaymentMethodName(String paymentMethod) {
        if (paymentMethod == null) return "未知";
        switch (paymentMethod) {
            case "cash":
                return "现金";
            case "wechat":
                return "微信支付";
            case "alipay":
                return "支付宝";
            case "card":
                return "刷卡";
            default:
                return paymentMethod;
        }
    }
}

package com.pindou.timer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.entity.ConsumptionRecord;
import com.pindou.timer.mapper.ConsumptionRecordMapper;
import com.pindou.timer.service.ConsumptionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消费记录Service实现类
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Service
public class ConsumptionRecordServiceImpl extends ServiceImpl<ConsumptionRecordMapper, ConsumptionRecord> implements ConsumptionRecordService {

    @Override
    public PageResult<Map<String, Object>> getConsumptionRecords(Long memberId, Integer page, Integer pageSize) {
        log.info("查询消费记录: memberId={}, page={}, pageSize={}", memberId, page, pageSize);

        LambdaQueryWrapper<ConsumptionRecord> wrapper = new LambdaQueryWrapper<>();
        if (memberId != null) {
            wrapper.eq(ConsumptionRecord::getMemberId, memberId);
        }
        wrapper.orderByDesc(ConsumptionRecord::getCreatedAt);

        Page<ConsumptionRecord> recordPage = new Page<>(page, pageSize);
        Page<ConsumptionRecord> resultPage = page(recordPage, wrapper);

        List<Map<String, Object>> records = resultPage.getRecords().stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());

        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setList(records);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage(page);
        pageResult.setPageSize(pageSize);

        return pageResult;
    }

    /**
     * 转换为Map
     */
    private Map<String, Object> convertToMap(ConsumptionRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("memberId", record.getMemberId());
        map.put("memberName", record.getMemberName());
        map.put("memberPhone", record.getMemberPhone());
        map.put("orderId", record.getOrderId());
        map.put("amount", record.getAmount());
        map.put("balanceBefore", record.getBalanceBefore());
        map.put("balanceAfter", record.getBalanceAfter());
        map.put("remark", record.getRemark());
        map.put("createdAt", record.getCreatedAt());
        return map;
    }
}

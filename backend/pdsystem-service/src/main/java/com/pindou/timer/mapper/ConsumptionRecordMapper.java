package com.pindou.timer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pindou.timer.entity.ConsumptionRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消费记录Mapper接口
 *
 * @author pindou
 * @since 1.0.0
 */
@Mapper
public interface ConsumptionRecordMapper extends BaseMapper<ConsumptionRecord> {
}

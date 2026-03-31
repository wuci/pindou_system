package com.pindou.timer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pindou.timer.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper接口
 *
 * @author wuci
 * @date 2026-03-28
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}

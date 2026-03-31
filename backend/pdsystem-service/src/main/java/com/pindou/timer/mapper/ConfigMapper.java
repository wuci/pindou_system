package com.pindou.timer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pindou.timer.entity.Config;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置Mapper接口
 *
 * @author wuci
 * @date 2026-03-28
 */
@Mapper
public interface ConfigMapper extends BaseMapper<Config> {
}

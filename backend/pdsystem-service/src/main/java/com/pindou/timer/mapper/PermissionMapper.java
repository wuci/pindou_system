package com.pindou.timer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pindou.timer.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限配置Mapper接口
 *
 * @author wuci
 * @date 2026-04-03
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}

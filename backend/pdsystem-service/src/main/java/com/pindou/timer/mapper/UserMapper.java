package com.pindou.timer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pindou.timer.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author pindou
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

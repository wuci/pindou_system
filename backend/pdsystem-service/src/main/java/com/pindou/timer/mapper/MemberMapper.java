package com.pindou.timer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pindou.timer.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员Mapper接口
 *
 * @author pindou
 * @since 1.0.0
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}

package com.pindou.timer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pindou.timer.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 *
 * @author pindou
 * @since 1.0.0
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}

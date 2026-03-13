package com.campus.trading.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trading.module.order.entity.OrderLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderLogMapper extends BaseMapper<OrderLogEntity> {
}

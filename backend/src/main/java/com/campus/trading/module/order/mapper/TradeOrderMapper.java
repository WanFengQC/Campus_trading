package com.campus.trading.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trading.module.order.entity.TradeOrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrderEntity> {
}

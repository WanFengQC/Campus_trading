package com.campus.trading.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trading.module.chat.entity.ChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {
}

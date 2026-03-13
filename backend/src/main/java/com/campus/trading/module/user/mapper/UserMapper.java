package com.campus.trading.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trading.module.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}

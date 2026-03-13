package com.campus.trading.module.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_auth")
public class UserAuthEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String authType;

    private String authKey;

    private String authSecret;

    private LocalDateTime createdAt;
}

package com.campus.trading.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`user`")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String nickname;

    private String avatarUrl;

    private Integer status;

    private String auditStatus;

    private String auditNote;

    private LocalDateTime auditTime;

    private LocalDateTime createdAt;
}

package com.campus.trading.module.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessageEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private Integer readStatus;

    private LocalDateTime createdAt;
}

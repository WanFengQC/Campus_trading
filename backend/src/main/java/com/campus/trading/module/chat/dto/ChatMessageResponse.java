package com.campus.trading.module.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {

    private Long messageId;

    private Long fromUserId;

    private String fromUserName;

    private Long toUserId;

    private String content;

    private boolean mine;

    private Integer readStatus;

    private LocalDateTime createdAt;
}

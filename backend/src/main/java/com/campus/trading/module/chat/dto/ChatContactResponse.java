package com.campus.trading.module.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatContactResponse {

    private Long sessionId;

    private Long peerUserId;

    private String peerName;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private Long unreadCount;
}

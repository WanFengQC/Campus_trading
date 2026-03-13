package com.campus.trading.module.chat.service;

import com.campus.trading.module.chat.dto.ChatContactResponse;
import com.campus.trading.module.chat.dto.ChatMessageResponse;

import java.util.List;

public interface ChatService {

    List<ChatContactResponse> listContacts(Long userId);

    List<ChatMessageResponse> listConversation(Long userId, Long peerUserId, Integer limit);

    Long sendMessage(Long fromUserId, Long toUserId, String content);

    void markConversationRead(Long userId, Long peerUserId);
}

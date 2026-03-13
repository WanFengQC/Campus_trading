package com.campus.trading.module.chat.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.security.SecurityUtils;
import com.campus.trading.module.chat.dto.ChatContactResponse;
import com.campus.trading.module.chat.dto.ChatMessageResponse;
import com.campus.trading.module.chat.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/contacts")
    public ApiResponse<List<ChatContactResponse>> contacts() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(chatService.listContacts(userId));
    }

    @GetMapping("/messages")
    public ApiResponse<List<ChatMessageResponse>> messages(@RequestParam("peerUserId") Long peerUserId,
                                                           @RequestParam(value = "limit", required = false) Integer limit) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(chatService.listConversation(userId, peerUserId, limit));
    }

    @PostMapping("/send")
    public ApiResponse<Long> send(@RequestParam("toUserId") Long toUserId,
                                  @RequestParam("content") String content) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(chatService.sendMessage(userId, toUserId, content));
    }

    @PostMapping("/read")
    public ApiResponse<Void> read(@RequestParam("peerUserId") Long peerUserId) {
        Long userId = SecurityUtils.getCurrentUserId();
        chatService.markConversationRead(userId, peerUserId);
        return ApiResponse.success(null);
    }
}

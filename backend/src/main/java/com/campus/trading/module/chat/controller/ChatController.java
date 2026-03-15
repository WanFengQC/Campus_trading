package com.campus.trading.module.chat.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.chat.dto.ChatContactResponse;
import com.campus.trading.module.chat.dto.ChatMessageResponse;
import com.campus.trading.module.chat.service.ChatService;
import jakarta.servlet.http.HttpSession;
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
    public ApiResponse<List<ChatContactResponse>> contacts(HttpSession session) {
        Long userId = getLoginUserId(session);
        return ApiResponse.success(chatService.listContacts(userId));
    }

    @GetMapping("/messages")
    public ApiResponse<List<ChatMessageResponse>> messages(@RequestParam("peerUserId") Long peerUserId,
                                                           @RequestParam(value = "limit", required = false) Integer limit,
                                                           HttpSession session) {
        Long userId = getLoginUserId(session);
        return ApiResponse.success(chatService.listConversation(userId, peerUserId, limit));
    }

    @PostMapping("/send")
    public ApiResponse<Long> send(@RequestParam("toUserId") Long toUserId,
                                  @RequestParam("content") String content,
                                  HttpSession session) {
        Long userId = getLoginUserId(session);
        return ApiResponse.success(chatService.sendMessage(userId, toUserId, content));
    }

    @PostMapping("/read")
    public ApiResponse<Void> read(@RequestParam("peerUserId") Long peerUserId,
                                  HttpSession session) {
        Long userId = getLoginUserId(session);
        chatService.markConversationRead(userId, peerUserId);
        return ApiResponse.success(null);
    }

    private Long getLoginUserId(HttpSession session) {
        Object value = session.getAttribute(WebSessionKeys.LOGIN_USER_ID);
        if (value instanceof Long userId) {
            return userId;
        }
        if (value instanceof Integer userId) {
            return userId.longValue();
        }
        throw new BusinessException("未登录或登录已过期");
    }
}

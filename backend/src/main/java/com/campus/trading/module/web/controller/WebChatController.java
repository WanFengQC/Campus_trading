package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.chat.dto.ChatContactResponse;
import com.campus.trading.module.chat.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebChatController {

    private final ChatService chatService;

    public WebChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "peerUserId", required = false) Long peerUserId,
                       Model model,
                       HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        List<ChatContactResponse> contacts = chatService.listContacts(userId);
        Long selectedPeerId = peerUserId;
        if (selectedPeerId == null && !contacts.isEmpty()) {
            selectedPeerId = contacts.get(0).getPeerUserId();
        }

        if (selectedPeerId != null) {
            chatService.markConversationRead(userId, selectedPeerId);
            model.addAttribute("messages", chatService.listConversation(userId, selectedPeerId, 100));
        }

        model.addAttribute("contacts", contacts);
        model.addAttribute("selectedPeerId", selectedPeerId);
        return "pages/chat";
    }

    @PostMapping("/chat/send")
    public String send(@RequestParam("toUserId") Long toUserId,
                       @RequestParam("content") String content,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            chatService.sendMessage(userId, toUserId, content);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/chat?peerUserId=" + toUserId;
    }

    private Long getLoginUserId(HttpSession session) {
        Object value = session.getAttribute(WebSessionKeys.LOGIN_USER_ID);
        if (value instanceof Long userId) {
            return userId;
        }
        if (value instanceof Integer userId) {
            return userId.longValue();
        }
        return null;
    }
}

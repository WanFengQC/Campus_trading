package com.campus.trading.module.web.controller;

import com.campus.trading.common.web.WebSessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.campus.trading.module.web.controller")
public class WebViewModelAdvice {

    @ModelAttribute
    public void applyCommonModel(Model model, HttpSession session) {
        if (model.containsAttribute("loggedIn")) {
            return;
        }
        model.addAttribute("loggedIn", getLoginUserId(session) != null);
    }

    private Long getLoginUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
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

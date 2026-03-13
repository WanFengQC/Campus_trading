package com.campus.trading.module.web.controller;

import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.browse.service.BrowseHistoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebBrowseHistoryController {

    private final BrowseHistoryService browseHistoryService;

    public WebBrowseHistoryController(BrowseHistoryService browseHistoryService) {
        this.browseHistoryService = browseHistoryService;
    }

    @GetMapping("/history")
    public String historyPage(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("items", browseHistoryService.listUserHistory(userId));
        return "pages/history";
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

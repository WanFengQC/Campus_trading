package com.campus.trading.module.web.controller;

import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.notice.service.NoticeService;
import com.campus.trading.module.recommend.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebHomeController {

    private final NoticeService noticeService;
    private final RecommendationService recommendationService;

    public WebHomeController(NoticeService noticeService,
                             RecommendationService recommendationService) {
        this.noticeService = noticeService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        model.addAttribute("loggedIn", userId != null);
        model.addAttribute("recommendations", userId == null
            ? recommendationService.listHotRecommendations(6)
            : recommendationService.listPersonalRecommendations(userId, 6));
        model.addAttribute("notices", noticeService.listPublished(3));
        return "pages/index";
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

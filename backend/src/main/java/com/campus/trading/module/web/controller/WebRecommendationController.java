package com.campus.trading.module.web.controller;

import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.recommend.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class WebRecommendationController {

    private final RecommendationService recommendationService;

    public WebRecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/recommendations")
    public String recommendationPage(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        boolean loggedIn = userId != null;

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("personalItems", loggedIn
            ? recommendationService.listPersonalRecommendations(userId, 12)
            : new ArrayList<>());
        model.addAttribute("hotItems", recommendationService.listHotRecommendations(12));
        return "pages/recommendations";
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

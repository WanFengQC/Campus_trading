package com.campus.trading.module.recommend.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.recommend.dto.RecommendationItemResponse;
import com.campus.trading.module.recommend.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/personal")
    public ApiResponse<List<RecommendationItemResponse>> personal(@RequestParam(value = "limit", required = false) Integer limit,
                                                                  HttpSession session) {
        int finalLimit = resolveLimit(limit);
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return ApiResponse.success(recommendationService.listHotRecommendations(finalLimit));
        }
        return ApiResponse.success(recommendationService.listPersonalRecommendations(userId, finalLimit));
    }

    @GetMapping("/hot")
    public ApiResponse<List<RecommendationItemResponse>> hot(@RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(recommendationService.listHotRecommendations(resolveLimit(limit)));
    }

    private int resolveLimit(Integer limit) {
        return limit == null ? 12 : limit;
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

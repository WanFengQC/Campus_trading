package com.campus.trading.module.recommend.service;

import com.campus.trading.module.recommend.dto.RecommendationItemResponse;

import java.util.List;

public interface RecommendationService {

    List<RecommendationItemResponse> listPersonalRecommendations(Long userId, int limit);

    List<RecommendationItemResponse> listHotRecommendations(int limit);
}

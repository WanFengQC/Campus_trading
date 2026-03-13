package com.campus.trading.module.favorite.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.security.SecurityUtils;
import com.campus.trading.module.favorite.dto.FavoriteItemResponse;
import com.campus.trading.module.favorite.service.FavoriteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/toggle")
    public ApiResponse<Boolean> toggle(@RequestParam("goodsId") Long goodsId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(favoriteService.toggleFavorite(userId, goodsId));
    }

    @GetMapping("/me")
    public ApiResponse<List<FavoriteItemResponse>> myFavorites() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(favoriteService.listUserFavorites(userId));
    }

    @GetMapping("/ids")
    public ApiResponse<Set<Long>> myFavoriteIds() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(favoriteService.listFavoriteGoodsIds(userId));
    }
}

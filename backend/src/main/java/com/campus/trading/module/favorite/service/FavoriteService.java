package com.campus.trading.module.favorite.service;

import com.campus.trading.module.favorite.dto.FavoriteItemResponse;

import java.util.List;
import java.util.Set;

public interface FavoriteService {

    boolean toggleFavorite(Long userId, Long goodsId);

    boolean isFavorited(Long userId, Long goodsId);

    Set<Long> listFavoriteGoodsIds(Long userId);

    List<FavoriteItemResponse> listUserFavorites(Long userId);
}

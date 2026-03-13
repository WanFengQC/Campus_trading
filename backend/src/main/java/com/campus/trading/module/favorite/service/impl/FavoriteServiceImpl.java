package com.campus.trading.module.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.category.entity.CategoryEntity;
import com.campus.trading.module.category.mapper.CategoryMapper;
import com.campus.trading.module.favorite.dto.FavoriteItemResponse;
import com.campus.trading.module.favorite.entity.FavoriteEntity;
import com.campus.trading.module.favorite.mapper.FavoriteMapper;
import com.campus.trading.module.favorite.service.FavoriteService;
import com.campus.trading.module.goods.entity.GoodsEntity;
import com.campus.trading.module.goods.mapper.GoodsMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper,
                               GoodsMapper goodsMapper,
                               CategoryMapper categoryMapper) {
        this.favoriteMapper = favoriteMapper;
        this.goodsMapper = goodsMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public boolean toggleFavorite(Long userId, Long goodsId) {
        GoodsEntity goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        FavoriteEntity existing = favoriteMapper.selectOne(new LambdaQueryWrapper<FavoriteEntity>()
            .eq(FavoriteEntity::getUserId, userId)
            .eq(FavoriteEntity::getGoodsId, goodsId)
            .last("limit 1"));
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        }

        FavoriteEntity favorite = new FavoriteEntity();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favoriteMapper.insert(favorite);
        return true;
    }

    @Override
    public boolean isFavorited(Long userId, Long goodsId) {
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<FavoriteEntity>()
            .eq(FavoriteEntity::getUserId, userId)
            .eq(FavoriteEntity::getGoodsId, goodsId));
        return count != null && count > 0;
    }

    @Override
    public Set<Long> listFavoriteGoodsIds(Long userId) {
        List<FavoriteEntity> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<FavoriteEntity>()
            .eq(FavoriteEntity::getUserId, userId));
        Set<Long> goodsIds = new LinkedHashSet<>();
        for (FavoriteEntity favorite : favorites) {
            goodsIds.add(favorite.getGoodsId());
        }
        return goodsIds;
    }

    @Override
    public List<FavoriteItemResponse> listUserFavorites(Long userId) {
        List<FavoriteEntity> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<FavoriteEntity>()
            .eq(FavoriteEntity::getUserId, userId)
            .orderByDesc(FavoriteEntity::getCreatedAt));
        if (favorites.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> goodsIds = new LinkedHashSet<>();
        for (FavoriteEntity favorite : favorites) {
            goodsIds.add(favorite.getGoodsId());
        }

        Map<Long, GoodsEntity> goodsMap = new HashMap<>();
        for (GoodsEntity goods : goodsMapper.selectBatchIds(goodsIds)) {
            goodsMap.put(goods.getId(), goods);
        }

        Map<Long, String> categoryNames = new HashMap<>();
        for (CategoryEntity category : categoryMapper.selectList(null)) {
            categoryNames.put(category.getId(), category.getName());
        }

        List<FavoriteItemResponse> results = new ArrayList<>();
        for (FavoriteEntity favorite : favorites) {
            GoodsEntity goods = goodsMap.get(favorite.getGoodsId());
            if (goods == null) {
                continue;
            }
            results.add(FavoriteItemResponse.builder()
                .favoriteId(favorite.getId())
                .goodsId(goods.getId())
                .title(goods.getTitle())
                .description(goods.getDescription())
                .price(goods.getPrice())
                .conditionLevel(goods.getConditionLevel())
                .coverImageUrl(goods.getCoverImageUrl())
                .categoryName(categoryNames.getOrDefault(goods.getCategoryId(), "未分类"))
                .status(goods.getStatus())
                .favoritedAt(favorite.getCreatedAt())
                .build());
        }
        return results;
    }
}

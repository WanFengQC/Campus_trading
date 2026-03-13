package com.campus.trading.module.browse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.module.browse.dto.BrowseHistoryItemResponse;
import com.campus.trading.module.browse.entity.BrowseHistoryEntity;
import com.campus.trading.module.browse.mapper.BrowseHistoryMapper;
import com.campus.trading.module.browse.service.BrowseHistoryService;
import com.campus.trading.module.category.entity.CategoryEntity;
import com.campus.trading.module.category.mapper.CategoryMapper;
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
public class BrowseHistoryServiceImpl implements BrowseHistoryService {

    private final BrowseHistoryMapper browseHistoryMapper;
    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;

    public BrowseHistoryServiceImpl(BrowseHistoryMapper browseHistoryMapper,
                                    GoodsMapper goodsMapper,
                                    CategoryMapper categoryMapper) {
        this.browseHistoryMapper = browseHistoryMapper;
        this.goodsMapper = goodsMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public void recordView(Long userId, Long goodsId) {
        BrowseHistoryEntity existing = browseHistoryMapper.selectOne(new LambdaQueryWrapper<BrowseHistoryEntity>()
            .eq(BrowseHistoryEntity::getUserId, userId)
            .eq(BrowseHistoryEntity::getGoodsId, goodsId)
            .last("limit 1"));

        if (existing != null) {
            browseHistoryMapper.deleteById(existing.getId());
        }

        BrowseHistoryEntity history = new BrowseHistoryEntity();
        history.setUserId(userId);
        history.setGoodsId(goodsId);
        browseHistoryMapper.insert(history);
    }

    @Override
    public List<BrowseHistoryItemResponse> listUserHistory(Long userId) {
        List<BrowseHistoryEntity> histories = browseHistoryMapper.selectList(new LambdaQueryWrapper<BrowseHistoryEntity>()
            .eq(BrowseHistoryEntity::getUserId, userId)
            .orderByDesc(BrowseHistoryEntity::getViewedAt)
            .last("limit 100"));
        if (histories.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> goodsIds = new LinkedHashSet<>();
        for (BrowseHistoryEntity history : histories) {
            goodsIds.add(history.getGoodsId());
        }

        Map<Long, GoodsEntity> goodsMap = new HashMap<>();
        for (GoodsEntity goods : goodsMapper.selectBatchIds(goodsIds)) {
            goodsMap.put(goods.getId(), goods);
        }

        Map<Long, String> categoryNames = new HashMap<>();
        for (CategoryEntity category : categoryMapper.selectList(null)) {
            categoryNames.put(category.getId(), category.getName());
        }

        List<BrowseHistoryItemResponse> results = new ArrayList<>();
        for (BrowseHistoryEntity history : histories) {
            GoodsEntity goods = goodsMap.get(history.getGoodsId());
            if (goods == null) {
                continue;
            }
            results.add(BrowseHistoryItemResponse.builder()
                .historyId(history.getId())
                .goodsId(goods.getId())
                .title(goods.getTitle())
                .description(goods.getDescription())
                .price(goods.getPrice())
                .conditionLevel(goods.getConditionLevel())
                .coverImageUrl(goods.getCoverImageUrl())
                .categoryName(categoryNames.getOrDefault(goods.getCategoryId(), "未分类"))
                .status(goods.getStatus())
                .viewedAt(history.getViewedAt())
                .build());
        }
        return results;
    }
}

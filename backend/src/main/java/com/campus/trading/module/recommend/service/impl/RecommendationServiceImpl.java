package com.campus.trading.module.recommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.module.browse.entity.BrowseHistoryEntity;
import com.campus.trading.module.browse.mapper.BrowseHistoryMapper;
import com.campus.trading.module.category.entity.CategoryEntity;
import com.campus.trading.module.category.mapper.CategoryMapper;
import com.campus.trading.module.favorite.entity.FavoriteEntity;
import com.campus.trading.module.favorite.mapper.FavoriteMapper;
import com.campus.trading.module.goods.entity.GoodsEntity;
import com.campus.trading.module.goods.mapper.GoodsMapper;
import com.campus.trading.module.order.entity.TradeOrderEntity;
import com.campus.trading.module.order.mapper.TradeOrderMapper;
import com.campus.trading.module.recommend.dto.RecommendationItemResponse;
import com.campus.trading.module.recommend.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final String STATUS_ON_SHELF = "ON_SHELF";
    private static final int DEFAULT_LIMIT = 12;
    private static final int MAX_LIMIT = 36;

    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;
    private final BrowseHistoryMapper browseHistoryMapper;
    private final FavoriteMapper favoriteMapper;
    private final TradeOrderMapper tradeOrderMapper;

    public RecommendationServiceImpl(GoodsMapper goodsMapper,
                                     CategoryMapper categoryMapper,
                                     BrowseHistoryMapper browseHistoryMapper,
                                     FavoriteMapper favoriteMapper,
                                     TradeOrderMapper tradeOrderMapper) {
        this.goodsMapper = goodsMapper;
        this.categoryMapper = categoryMapper;
        this.browseHistoryMapper = browseHistoryMapper;
        this.favoriteMapper = favoriteMapper;
        this.tradeOrderMapper = tradeOrderMapper;
    }

    @Override
    public List<RecommendationItemResponse> listPersonalRecommendations(Long userId, int limit) {
        int finalLimit = normalizeLimit(limit);
        if (userId == null) {
            return listHotRecommendations(finalLimit);
        }

        List<GoodsEntity> candidates = loadCandidateGoods(userId, 320);
        if (candidates.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, GoodsEntity> candidateMap = new HashMap<>();
        Set<Long> candidateGoodsIds = new LinkedHashSet<>();
        for (GoodsEntity candidate : candidates) {
            candidateMap.put(candidate.getId(), candidate);
            candidateGoodsIds.add(candidate.getId());
        }

        List<BrowseHistoryEntity> userBrowse = browseHistoryMapper.selectList(new LambdaQueryWrapper<BrowseHistoryEntity>()
            .eq(BrowseHistoryEntity::getUserId, userId)
            .orderByDesc(BrowseHistoryEntity::getViewedAt)
            .last("limit 120"));
        List<FavoriteEntity> userFavorites = favoriteMapper.selectList(new LambdaQueryWrapper<FavoriteEntity>()
            .eq(FavoriteEntity::getUserId, userId)
            .orderByDesc(FavoriteEntity::getCreatedAt)
            .last("limit 80"));
        List<TradeOrderEntity> userOrders = tradeOrderMapper.selectList(new LambdaQueryWrapper<TradeOrderEntity>()
            .eq(TradeOrderEntity::getBuyerId, userId)
            .orderByDesc(TradeOrderEntity::getCreatedAt)
            .last("limit 60"));

        Set<Long> signalGoodsIds = new LinkedHashSet<>();
        for (BrowseHistoryEntity item : userBrowse) {
            signalGoodsIds.add(item.getGoodsId());
        }
        for (FavoriteEntity item : userFavorites) {
            signalGoodsIds.add(item.getGoodsId());
        }
        for (TradeOrderEntity item : userOrders) {
            signalGoodsIds.add(item.getGoodsId());
        }

        Map<Long, GoodsEntity> signalGoodsMap = new HashMap<>();
        if (!signalGoodsIds.isEmpty()) {
            for (GoodsEntity signalGoods : goodsMapper.selectBatchIds(signalGoodsIds)) {
                signalGoodsMap.put(signalGoods.getId(), signalGoods);
            }
        }

        Map<Long, Double> categoryScores = new HashMap<>();
        Map<Long, Double> goodsScores = new HashMap<>();
        Set<Long> interactedGoodsIds = new HashSet<>();

        for (BrowseHistoryEntity item : userBrowse) {
            Long goodsId = item.getGoodsId();
            interactedGoodsIds.add(goodsId);
            goodsScores.merge(goodsId, 1.2, Double::sum);
            GoodsEntity signalGoods = signalGoodsMap.get(goodsId);
            if (signalGoods != null) {
                categoryScores.merge(signalGoods.getCategoryId(), 1.0, Double::sum);
            }
        }
        for (FavoriteEntity item : userFavorites) {
            Long goodsId = item.getGoodsId();
            interactedGoodsIds.add(goodsId);
            goodsScores.merge(goodsId, 2.3, Double::sum);
            GoodsEntity signalGoods = signalGoodsMap.get(goodsId);
            if (signalGoods != null) {
                categoryScores.merge(signalGoods.getCategoryId(), 2.0, Double::sum);
            }
        }
        for (TradeOrderEntity item : userOrders) {
            Long goodsId = item.getGoodsId();
            interactedGoodsIds.add(goodsId);
            goodsScores.merge(goodsId, 3.1, Double::sum);
            GoodsEntity signalGoods = signalGoodsMap.get(goodsId);
            if (signalGoods != null) {
                categoryScores.merge(signalGoods.getCategoryId(), 2.6, Double::sum);
            }
        }

        Map<Long, Integer> browseCounts = countByGoodsId(browseHistoryMapper.selectList(new LambdaQueryWrapper<BrowseHistoryEntity>()
            .in(BrowseHistoryEntity::getGoodsId, candidateGoodsIds)
            .last("limit 6000")), BrowseHistoryEntity::getGoodsId);
        Map<Long, Integer> favoriteCounts = countByGoodsId(favoriteMapper.selectList(new LambdaQueryWrapper<FavoriteEntity>()
            .in(FavoriteEntity::getGoodsId, candidateGoodsIds)
            .last("limit 6000")), FavoriteEntity::getGoodsId);
        Map<Long, Integer> orderCounts = countByGoodsId(tradeOrderMapper.selectList(new LambdaQueryWrapper<TradeOrderEntity>()
            .in(TradeOrderEntity::getGoodsId, candidateGoodsIds)
            .last("limit 6000")), TradeOrderEntity::getGoodsId);

        Map<Long, String> categoryNames = loadCategoryNames();
        LocalDateTime now = LocalDateTime.now();
        List<ScoredGoods> scored = new ArrayList<>();
        for (GoodsEntity goods : candidates) {
            double score = 1.0;
            score += categoryScores.getOrDefault(goods.getCategoryId(), 0.0);
            score += goodsScores.getOrDefault(goods.getId(), 0.0) * 0.35;
            score += popularityScore(goods.getId(), browseCounts, favoriteCounts, orderCounts);

            double recency = recencyScore(goods.getCreatedAt(), now);
            score += recency;
            score += interactedGoodsIds.contains(goods.getId()) ? -1.5 : 0.8;

            String reason;
            if (categoryScores.getOrDefault(goods.getCategoryId(), 0.0) >= 2.0) {
                reason = "根据你的浏览/收藏偏好推荐";
            } else if (popularityScore(goods.getId(), browseCounts, favoriteCounts, orderCounts) >= 3.0) {
                reason = "校园热度较高，近期关注人数多";
            } else if (recency >= 2.0) {
                reason = "最新上架，值得优先关注";
            } else if (interactedGoodsIds.contains(goods.getId())) {
                reason = "你近期看过这件商品";
            } else {
                reason = "为你精选的同类好物";
            }

            scored.add(new ScoredGoods(goods, score, reason));
        }

        scored.sort((a, b) -> {
            int byScore = Double.compare(b.score(), a.score());
            if (byScore != 0) {
                return byScore;
            }
            LocalDateTime aTime = a.goods().getCreatedAt();
            LocalDateTime bTime = b.goods().getCreatedAt();
            if (aTime == null && bTime == null) {
                return 0;
            }
            if (aTime == null) {
                return 1;
            }
            if (bTime == null) {
                return -1;
            }
            return bTime.compareTo(aTime);
        });

        return toResponses(scored, categoryNames, finalLimit);
    }

    @Override
    public List<RecommendationItemResponse> listHotRecommendations(int limit) {
        int finalLimit = normalizeLimit(limit);
        List<GoodsEntity> candidates = loadCandidateGoods(null, 260);
        if (candidates.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> candidateGoodsIds = new LinkedHashSet<>();
        for (GoodsEntity candidate : candidates) {
            candidateGoodsIds.add(candidate.getId());
        }

        Map<Long, Integer> browseCounts = countByGoodsId(browseHistoryMapper.selectList(new LambdaQueryWrapper<BrowseHistoryEntity>()
            .in(BrowseHistoryEntity::getGoodsId, candidateGoodsIds)
            .last("limit 6000")), BrowseHistoryEntity::getGoodsId);
        Map<Long, Integer> favoriteCounts = countByGoodsId(favoriteMapper.selectList(new LambdaQueryWrapper<FavoriteEntity>()
            .in(FavoriteEntity::getGoodsId, candidateGoodsIds)
            .last("limit 6000")), FavoriteEntity::getGoodsId);
        Map<Long, Integer> orderCounts = countByGoodsId(tradeOrderMapper.selectList(new LambdaQueryWrapper<TradeOrderEntity>()
            .in(TradeOrderEntity::getGoodsId, candidateGoodsIds)
            .last("limit 6000")), TradeOrderEntity::getGoodsId);

        Map<Long, String> categoryNames = loadCategoryNames();
        LocalDateTime now = LocalDateTime.now();
        List<ScoredGoods> scored = new ArrayList<>();
        for (GoodsEntity goods : candidates) {
            double recency = recencyScore(goods.getCreatedAt(), now);
            double popularity = popularityScore(goods.getId(), browseCounts, favoriteCounts, orderCounts);
            double score = recency + popularity;

            String reason;
            if (popularity >= 3.0) {
                reason = "热门商品，近期关注度高";
            } else if (recency >= 2.0) {
                reason = "新品上架，更新鲜";
            } else {
                reason = "综合热度推荐";
            }

            scored.add(new ScoredGoods(goods, score, reason));
        }

        scored.sort((a, b) -> Double.compare(b.score(), a.score()));
        return toResponses(scored, categoryNames, finalLimit);
    }

    private List<GoodsEntity> loadCandidateGoods(Long userId, int maxCount) {
        LambdaQueryWrapper<GoodsEntity> wrapper = new LambdaQueryWrapper<GoodsEntity>()
            .eq(GoodsEntity::getStatus, STATUS_ON_SHELF)
            .orderByDesc(GoodsEntity::getCreatedAt)
            .last("limit " + Math.max(1, maxCount));
        if (userId != null) {
            wrapper.ne(GoodsEntity::getSellerId, userId);
        }
        return goodsMapper.selectList(wrapper);
    }

    private Map<Long, String> loadCategoryNames() {
        Map<Long, String> categoryNames = new HashMap<>();
        for (CategoryEntity category : categoryMapper.selectList(null)) {
            categoryNames.put(category.getId(), category.getName());
        }
        return categoryNames;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private double recencyScore(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null) {
            return 0.0;
        }
        long days = Math.max(0, ChronoUnit.DAYS.between(createdAt, now));
        if (days <= 3) {
            return 2.8;
        }
        if (days <= 7) {
            return 2.0;
        }
        if (days <= 30) {
            return 1.0;
        }
        return 0.0;
    }

    private double popularityScore(Long goodsId,
                                   Map<Long, Integer> browseCounts,
                                   Map<Long, Integer> favoriteCounts,
                                   Map<Long, Integer> orderCounts) {
        int browseCount = browseCounts.getOrDefault(goodsId, 0);
        int favoriteCount = favoriteCounts.getOrDefault(goodsId, 0);
        int orderCount = orderCounts.getOrDefault(goodsId, 0);

        double browseScore = Math.min(2.0, browseCount * 0.08);
        double favoriteScore = Math.min(2.6, favoriteCount * 0.28);
        double orderScore = Math.min(3.2, orderCount * 0.55);
        return browseScore + favoriteScore + orderScore;
    }

    private <T> Map<Long, Integer> countByGoodsId(List<T> rows, java.util.function.Function<T, Long> goodsIdExtractor) {
        Map<Long, Integer> counts = new HashMap<>();
        for (T row : rows) {
            Long goodsId = goodsIdExtractor.apply(row);
            if (goodsId == null) {
                continue;
            }
            counts.merge(goodsId, 1, Integer::sum);
        }
        return counts;
    }

    private List<RecommendationItemResponse> toResponses(List<ScoredGoods> scored,
                                                         Map<Long, String> categoryNames,
                                                         int limit) {
        List<RecommendationItemResponse> results = new ArrayList<>();
        int size = Math.min(limit, scored.size());
        for (int i = 0; i < size; i++) {
            ScoredGoods row = scored.get(i);
            GoodsEntity goods = row.goods();
            results.add(RecommendationItemResponse.builder()
                .goodsId(goods.getId())
                .title(goods.getTitle())
                .description(goods.getDescription())
                .price(goods.getPrice())
                .conditionLevel(goods.getConditionLevel())
                .coverImageUrl(goods.getCoverImageUrl())
                .categoryName(categoryNames.getOrDefault(goods.getCategoryId(), "未分类"))
                .recommendReason(row.reason())
                .createdAt(goods.getCreatedAt())
                .build());
        }
        return results;
    }

    private record ScoredGoods(GoodsEntity goods, double score, String reason) {
    }
}

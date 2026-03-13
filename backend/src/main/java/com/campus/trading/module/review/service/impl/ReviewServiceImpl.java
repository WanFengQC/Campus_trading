package com.campus.trading.module.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.goods.entity.GoodsEntity;
import com.campus.trading.module.goods.mapper.GoodsMapper;
import com.campus.trading.module.order.entity.TradeOrderEntity;
import com.campus.trading.module.order.mapper.TradeOrderMapper;
import com.campus.trading.module.review.dto.ReviewResponse;
import com.campus.trading.module.review.entity.ReviewEntity;
import com.campus.trading.module.review.mapper.ReviewMapper;
import com.campus.trading.module.review.service.ReviewService;
import com.campus.trading.module.user.entity.UserEntity;
import com.campus.trading.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final String ORDER_STATUS_COMPLETED = "COMPLETED";

    private final ReviewMapper reviewMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;

    public ReviewServiceImpl(ReviewMapper reviewMapper,
                             TradeOrderMapper tradeOrderMapper,
                             GoodsMapper goodsMapper,
                             UserMapper userMapper) {
        this.reviewMapper = reviewMapper;
        this.tradeOrderMapper = tradeOrderMapper;
        this.goodsMapper = goodsMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Long createOrderReview(Long reviewerId, Long orderId, Integer score, String content) {
        TradeOrderEntity order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!ORDER_STATUS_COMPLETED.equals(order.getStatus())) {
            throw new BusinessException("仅已完成订单可评价");
        }
        if (!reviewerId.equals(order.getBuyerId()) && !reviewerId.equals(order.getSellerId())) {
            throw new BusinessException("无权评价该订单");
        }
        if (score == null || score < 1 || score > 5) {
            throw new BusinessException("评分范围为 1~5");
        }

        ReviewEntity existing = reviewMapper.selectOne(new LambdaQueryWrapper<ReviewEntity>()
            .eq(ReviewEntity::getOrderId, orderId)
            .eq(ReviewEntity::getReviewerId, reviewerId)
            .last("limit 1"));
        if (existing != null) {
            throw new BusinessException("该订单你已评价过");
        }

        ReviewEntity review = new ReviewEntity();
        review.setOrderId(orderId);
        review.setGoodsId(order.getGoodsId());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(reviewerId.equals(order.getBuyerId()) ? order.getSellerId() : order.getBuyerId());
        review.setScore(score);
        review.setContent(normalizeContent(content));
        reviewMapper.insert(review);
        return review.getId();
    }

    @Override
    public ReviewResponse getUserOrderReview(Long reviewerId, Long orderId) {
        ReviewEntity review = reviewMapper.selectOne(new LambdaQueryWrapper<ReviewEntity>()
            .eq(ReviewEntity::getOrderId, orderId)
            .eq(ReviewEntity::getReviewerId, reviewerId)
            .last("limit 1"));
        if (review == null) {
            return null;
        }
        return toResponses(List.of(review)).get(0);
    }

    @Override
    public List<ReviewResponse> listGoodsReviews(Long goodsId) {
        List<ReviewEntity> reviews = reviewMapper.selectList(new LambdaQueryWrapper<ReviewEntity>()
            .eq(ReviewEntity::getGoodsId, goodsId)
            .orderByDesc(ReviewEntity::getCreatedAt));
        return toResponses(reviews);
    }

    @Override
    public List<ReviewResponse> listUserWrittenReviews(Long userId) {
        List<ReviewEntity> reviews = reviewMapper.selectList(new LambdaQueryWrapper<ReviewEntity>()
            .eq(ReviewEntity::getReviewerId, userId)
            .orderByDesc(ReviewEntity::getCreatedAt));
        return toResponses(reviews);
    }

    @Override
    public List<ReviewResponse> listUserReceivedReviews(Long userId) {
        List<ReviewEntity> reviews = reviewMapper.selectList(new LambdaQueryWrapper<ReviewEntity>()
            .eq(ReviewEntity::getRevieweeId, userId)
            .orderByDesc(ReviewEntity::getCreatedAt));
        return toResponses(reviews);
    }

    @Override
    public Set<Long> listReviewedOrderIds(Long reviewerId, List<Long> orderIds) {
        Set<Long> result = new LinkedHashSet<>();
        if (orderIds == null || orderIds.isEmpty()) {
            return result;
        }
        List<ReviewEntity> reviews = reviewMapper.selectList(new LambdaQueryWrapper<ReviewEntity>()
            .eq(ReviewEntity::getReviewerId, reviewerId)
            .in(ReviewEntity::getOrderId, orderIds));
        for (ReviewEntity review : reviews) {
            result.add(review.getOrderId());
        }
        return result;
    }

    private List<ReviewResponse> toResponses(List<ReviewEntity> reviews) {
        List<ReviewResponse> result = new ArrayList<>();
        if (reviews == null || reviews.isEmpty()) {
            return result;
        }

        Set<Long> goodsIds = new LinkedHashSet<>();
        Set<Long> userIds = new LinkedHashSet<>();
        for (ReviewEntity review : reviews) {
            goodsIds.add(review.getGoodsId());
            userIds.add(review.getReviewerId());
            userIds.add(review.getRevieweeId());
        }

        Map<Long, GoodsEntity> goodsMap = new HashMap<>();
        for (GoodsEntity goods : goodsMapper.selectBatchIds(goodsIds)) {
            goodsMap.put(goods.getId(), goods);
        }

        Map<Long, UserEntity> userMap = new HashMap<>();
        for (UserEntity user : userMapper.selectBatchIds(userIds)) {
            userMap.put(user.getId(), user);
        }

        for (ReviewEntity review : reviews) {
            GoodsEntity goods = goodsMap.get(review.getGoodsId());
            UserEntity reviewer = userMap.get(review.getReviewerId());
            UserEntity reviewee = userMap.get(review.getRevieweeId());
            result.add(ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .goodsId(review.getGoodsId())
                .goodsTitle(goods == null ? "商品已删除" : goods.getTitle())
                .reviewerId(review.getReviewerId())
                .reviewerName(resolveDisplayName(reviewer))
                .revieweeId(review.getRevieweeId())
                .revieweeName(resolveDisplayName(reviewee))
                .score(review.getScore())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build());
        }
        return result;
    }

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String trimmed = content.trim();
        if (trimmed.length() <= 255) {
            return trimmed;
        }
        return trimmed.substring(0, 255);
    }

    private String resolveDisplayName(UserEntity user) {
        if (user == null) {
            return "未知用户";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername().trim();
        }
        return "未知用户";
    }
}

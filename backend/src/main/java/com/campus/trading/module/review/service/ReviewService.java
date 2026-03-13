package com.campus.trading.module.review.service;

import com.campus.trading.module.review.dto.ReviewResponse;

import java.util.List;
import java.util.Set;

public interface ReviewService {

    Long createOrderReview(Long reviewerId, Long orderId, Integer score, String content);

    ReviewResponse getUserOrderReview(Long reviewerId, Long orderId);

    List<ReviewResponse> listGoodsReviews(Long goodsId);

    List<ReviewResponse> listUserWrittenReviews(Long userId);

    List<ReviewResponse> listUserReceivedReviews(Long userId);

    Set<Long> listReviewedOrderIds(Long reviewerId, List<Long> orderIds);
}

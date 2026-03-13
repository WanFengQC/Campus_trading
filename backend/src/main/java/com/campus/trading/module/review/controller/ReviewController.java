package com.campus.trading.module.review.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.security.SecurityUtils;
import com.campus.trading.module.review.dto.ReviewResponse;
import com.campus.trading.module.review.service.ReviewService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/orders/{orderId}")
    public ApiResponse<Long> create(@PathVariable("orderId") Long orderId,
                                    @RequestParam("score") Integer score,
                                    @RequestParam(value = "content", required = false) String content) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(reviewService.createOrderReview(userId, orderId, score, content));
    }

    @GetMapping("/goods/{goodsId}")
    public ApiResponse<List<ReviewResponse>> goodsReviews(@PathVariable("goodsId") Long goodsId) {
        return ApiResponse.success(reviewService.listGoodsReviews(goodsId));
    }

    @GetMapping("/me/written")
    public ApiResponse<List<ReviewResponse>> myWritten() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(reviewService.listUserWrittenReviews(userId));
    }

    @GetMapping("/me/received")
    public ApiResponse<List<ReviewResponse>> myReceived() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(reviewService.listUserReceivedReviews(userId));
    }
}

package com.campus.trading.module.review.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {

    private Long id;

    private Long orderId;

    private Long goodsId;

    private String goodsTitle;

    private Long reviewerId;

    private String reviewerName;

    private Long revieweeId;

    private String revieweeName;

    private Integer score;

    private String content;

    private LocalDateTime createdAt;
}

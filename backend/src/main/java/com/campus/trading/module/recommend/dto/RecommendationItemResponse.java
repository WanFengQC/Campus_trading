package com.campus.trading.module.recommend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RecommendationItemResponse {

    private Long goodsId;

    private String title;

    private String description;

    private BigDecimal price;

    private String conditionLevel;

    private String coverImageUrl;

    private String categoryName;

    private String recommendReason;

    private LocalDateTime createdAt;
}

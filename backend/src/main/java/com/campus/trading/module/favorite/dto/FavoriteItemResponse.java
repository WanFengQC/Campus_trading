package com.campus.trading.module.favorite.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FavoriteItemResponse {

    private Long favoriteId;
    private Long goodsId;
    private String title;
    private String description;
    private BigDecimal price;
    private String conditionLevel;
    private String coverImageUrl;
    private String categoryName;
    private String status;
    private LocalDateTime favoritedAt;
}

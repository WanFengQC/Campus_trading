package com.campus.trading.module.browse.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BrowseHistoryItemResponse {

    private Long historyId;
    private Long goodsId;
    private String title;
    private String description;
    private BigDecimal price;
    private String conditionLevel;
    private String coverImageUrl;
    private String categoryName;
    private String status;
    private LocalDateTime viewedAt;
}

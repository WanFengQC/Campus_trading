package com.campus.trading.module.goods.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GoodsListItemResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String conditionLevel;
    private String contactInfo;
    private String coverImageUrl;
    private String categoryName;
    private String status;
    private String auditStatus;
    private String auditStatusLabel;
    private String auditNote;
    private LocalDateTime auditTime;
    private LocalDateTime createdAt;
}

package com.campus.trading.module.goods.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GoodsDetailResponse {

    private Long id;
    private Long sellerId;
    private String sellerName;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal price;
    private String conditionLevel;
    private String contactInfo;
    private String coverImageUrl;
    private List<String> imageUrls;
    private String status;
    private String auditStatus;
    private String auditStatusLabel;
    private String auditNote;
    private LocalDateTime auditTime;
    private LocalDateTime createdAt;
}

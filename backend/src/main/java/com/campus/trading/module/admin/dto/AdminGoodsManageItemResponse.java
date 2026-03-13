package com.campus.trading.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminGoodsManageItemResponse {

    private Long goodsId;

    private String title;

    private String categoryName;

    private String sellerName;

    private BigDecimal price;

    private String status;

    private String auditStatus;

    private String auditStatusLabel;

    private String auditNote;

    private LocalDateTime auditTime;

    private String contactInfo;

    private LocalDateTime createdAt;
}

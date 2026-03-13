package com.campus.trading.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminOrderManageItemResponse {

    private Long orderId;

    private String orderNo;

    private Long goodsId;

    private String goodsTitle;

    private String buyerName;

    private String sellerName;

    private BigDecimal amount;

    private String status;

    private String statusLabel;

    private LocalDateTime meetupTime;

    private String meetupLocation;

    private String meetupNote;

    private LocalDateTime createdAt;
}

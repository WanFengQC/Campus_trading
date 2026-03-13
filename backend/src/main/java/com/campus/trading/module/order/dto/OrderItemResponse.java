package com.campus.trading.module.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderItemResponse {

    private Long orderId;
    private String orderNo;
    private Long goodsId;
    private String goodsTitle;
    private String goodsCoverImageUrl;
    private BigDecimal amount;
    private String status;
    private String statusLabel;
    private String buyerRemark;
    private LocalDateTime meetupTime;
    private String meetupLocation;
    private String meetupNote;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private boolean buyerSide;
    private boolean sellerSide;
    private LocalDateTime createdAt;
}

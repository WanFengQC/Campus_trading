package com.campus.trading.module.cart.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CartItemResponse {

    private Long cartId;
    private Long goodsId;
    private String goodsTitle;
    private String goodsCoverImageUrl;
    private String goodsConditionLevel;
    private String goodsStatus;
    private String sellerName;
    private String contactInfo;
    private BigDecimal price;
    private LocalDateTime addedAt;
}

package com.campus.trading.module.goods.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsQueryRequest {

    private String keyword;

    private Long categoryId;

    private String conditionLevel;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;
}
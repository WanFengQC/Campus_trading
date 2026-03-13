package com.campus.trading.module.rental.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RentalItemSaveRequest {

    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal dailyRent;
    private BigDecimal deposit;
    private String contactInfo;
    private String coverImageUrl;
}

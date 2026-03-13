package com.campus.trading.module.rental.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RentalItemResponse {

    private Long id;
    private Long ownerId;
    private String ownerName;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal dailyRent;
    private BigDecimal deposit;
    private String contactInfo;
    private String coverImageUrl;
    private String status;
    private LocalDateTime createdAt;
}

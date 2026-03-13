package com.campus.trading.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminRentalOrderManageItemResponse {

    private Long orderId;

    private String orderNo;

    private Long rentalItemId;

    private String rentalTitle;

    private String renterName;

    private String ownerName;

    private BigDecimal dailyRent;

    private BigDecimal deposit;

    private BigDecimal totalAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private String statusLabel;

    private LocalDateTime createdAt;
}

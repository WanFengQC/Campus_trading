package com.campus.trading.module.rental.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RentalOrderResponse {

    private Long orderId;
    private String orderNo;
    private Long rentalItemId;
    private String rentalTitle;
    private String rentalCoverImageUrl;
    private Long renterId;
    private String renterName;
    private Long ownerId;
    private String ownerName;
    private BigDecimal dailyRent;
    private BigDecimal deposit;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private String renterRemark;
    private String status;
    private String statusLabel;
    private boolean renterSide;
    private boolean ownerSide;
    private LocalDateTime createdAt;
}

package com.campus.trading.module.donation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DonationItemResponse {

    private Long id;

    private Long donorId;

    private String donorName;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String description;

    private String contactInfo;

    private String pickupAddress;

    private String coverImageUrl;

    private String status;

    private String statusLabel;

    private LocalDateTime createdAt;
}

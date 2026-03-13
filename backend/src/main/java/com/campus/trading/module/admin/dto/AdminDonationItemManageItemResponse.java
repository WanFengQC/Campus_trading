package com.campus.trading.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminDonationItemManageItemResponse {

    private Long itemId;

    private String title;

    private String donorName;

    private String categoryName;

    private String contactInfo;

    private String pickupAddress;

    private String status;

    private String statusLabel;

    private LocalDateTime createdAt;
}

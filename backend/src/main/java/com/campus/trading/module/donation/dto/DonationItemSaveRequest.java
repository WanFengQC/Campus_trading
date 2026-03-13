package com.campus.trading.module.donation.dto;

import lombok.Data;

@Data
public class DonationItemSaveRequest {

    private Long categoryId;

    private String title;

    private String description;

    private String contactInfo;

    private String pickupAddress;

    private String coverImageUrl;
}

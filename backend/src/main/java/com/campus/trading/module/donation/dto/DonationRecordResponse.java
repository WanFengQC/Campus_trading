package com.campus.trading.module.donation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DonationRecordResponse {

    private Long recordId;

    private Long donationItemId;

    private String donationTitle;

    private String donationCoverImageUrl;

    private Long donorId;

    private String donorName;

    private Long claimerId;

    private String claimerName;

    private String claimRemark;

    private String status;

    private String statusLabel;

    private boolean donorSide;

    private boolean claimerSide;

    private LocalDateTime createdAt;
}

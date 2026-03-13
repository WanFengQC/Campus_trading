package com.campus.trading.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminDonationRecordManageItemResponse {

    private Long recordId;

    private Long donationItemId;

    private String donationTitle;

    private String donorName;

    private String claimerName;

    private String claimRemark;

    private String status;

    private String statusLabel;

    private LocalDateTime createdAt;
}

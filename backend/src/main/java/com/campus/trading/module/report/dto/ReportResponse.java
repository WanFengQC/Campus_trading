package com.campus.trading.module.report.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {

    private Long id;

    private Long reporterId;

    private String reporterName;

    private String targetType;

    private Long targetId;

    private String targetSummary;

    private String reason;

    private String detail;

    private String status;

    private String statusLabel;

    private String handleNote;

    private LocalDateTime handledAt;

    private LocalDateTime createdAt;
}

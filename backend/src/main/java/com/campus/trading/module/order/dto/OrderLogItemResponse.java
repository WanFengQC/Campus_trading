package com.campus.trading.module.order.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderLogItemResponse {

    private Long id;

    private Long orderId;

    private String action;

    private String actionLabel;

    private String fromStatus;

    private String fromStatusLabel;

    private String toStatus;

    private String toStatusLabel;

    private Long operatorUserId;

    private String operatorName;

    private String note;

    private LocalDateTime createdAt;
}

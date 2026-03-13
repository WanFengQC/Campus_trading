package com.campus.trading.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserManageItemResponse {

    private Long userId;

    private String username;

    private String nickname;

    private Integer status;

    private String auditStatus;

    private String auditStatusLabel;

    private String auditNote;

    private LocalDateTime auditTime;

    private LocalDateTime createdAt;
}

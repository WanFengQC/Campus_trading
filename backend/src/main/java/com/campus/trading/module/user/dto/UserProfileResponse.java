package com.campus.trading.module.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Integer status;
    private String auditStatus;
    private String auditNote;
    private LocalDateTime auditTime;
}

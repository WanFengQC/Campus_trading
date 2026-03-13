package com.campus.trading.module.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {

    private Long userId;

    private String username;

    private String nickname;

    private String auditStatus;

    private String message;
}

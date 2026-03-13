package com.campus.trading.module.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthTokenResponse {

    private String token;
    private String tokenType;
    private long expireSeconds;
    private Long userId;
    private String username;
    private String nickname;
}

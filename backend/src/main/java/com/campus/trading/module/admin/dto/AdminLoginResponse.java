package com.campus.trading.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginResponse {

    private Long adminId;

    private String username;

    private String nickname;
}

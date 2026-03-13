package com.campus.trading.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "学号/工号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

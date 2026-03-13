package com.campus.trading.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "学号/工号不能为空")
    @Size(min = 5, max = 32, message = "学号/工号长度需在 5 到 32 位之间")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "学号/工号仅支持字母、数字、下划线和中划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在 6 到 64 位之间")
    private String password;

    @Size(max = 64, message = "昵称长度不能超过 64 位")
    private String nickname;
}

package com.campus.trading.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过 64 位")
    private String nickname;

    @Size(max = 255, message = "头像地址长度不能超过 255")
    private String avatarUrl;
}

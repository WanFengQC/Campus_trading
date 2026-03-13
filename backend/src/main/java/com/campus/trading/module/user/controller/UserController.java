package com.campus.trading.module.user.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.security.SecurityUtils;
import com.campus.trading.module.user.dto.UpdateProfileRequest;
import com.campus.trading.module.user.dto.UserProfileResponse;
import com.campus.trading.module.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(userService.getCurrentUserProfile(userId));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(userService.updateCurrentUserProfile(userId, request));
    }
}

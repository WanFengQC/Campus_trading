package com.campus.trading.module.auth.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.module.auth.dto.AuthTokenResponse;
import com.campus.trading.module.auth.dto.LoginRequest;
import com.campus.trading.module.auth.dto.RegisterResponse;
import com.campus.trading.module.auth.dto.RegisterRequest;
import com.campus.trading.module.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}

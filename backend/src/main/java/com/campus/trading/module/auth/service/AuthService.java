package com.campus.trading.module.auth.service;

import com.campus.trading.module.auth.dto.AuthTokenResponse;
import com.campus.trading.module.auth.dto.LoginRequest;
import com.campus.trading.module.auth.dto.RegisterResponse;
import com.campus.trading.module.auth.dto.RegisterRequest;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthTokenResponse login(LoginRequest request);
}

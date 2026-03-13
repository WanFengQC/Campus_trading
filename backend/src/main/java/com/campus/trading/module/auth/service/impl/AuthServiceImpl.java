package com.campus.trading.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.security.JwtTokenProvider;
import com.campus.trading.config.properties.JwtProperties;
import com.campus.trading.module.auth.dto.AuthTokenResponse;
import com.campus.trading.module.auth.dto.LoginRequest;
import com.campus.trading.module.auth.dto.RegisterResponse;
import com.campus.trading.module.auth.dto.RegisterRequest;
import com.campus.trading.module.auth.entity.UserAuthEntity;
import com.campus.trading.module.auth.mapper.UserAuthMapper;
import com.campus.trading.module.auth.service.AuthService;
import com.campus.trading.module.user.entity.UserEntity;
import com.campus.trading.module.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String AUTH_TYPE_PASSWORD = "PASSWORD";
    private static final String USER_AUDIT_PENDING = "PENDING";
    private static final String USER_AUDIT_APPROVED = "APPROVED";
    private static final String USER_AUDIT_REJECTED = "REJECTED";

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(UserMapper userMapper,
                           UserAuthMapper userAuthMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           JwtProperties jwtProperties) {
        this.userMapper = userMapper;
        this.userAuthMapper = userAuthMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        if (existsUsername(username)) {
            throw new BusinessException("学号/工号已存在");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : username);
        user.setStatus(1);
        user.setAuditStatus(USER_AUDIT_PENDING);
        user.setAuditNote("待管理员审核");
        user.setAuditTime(null);
        userMapper.insert(user);

        UserAuthEntity userAuth = new UserAuthEntity();
        userAuth.setUserId(user.getId());
        userAuth.setAuthType(AUTH_TYPE_PASSWORD);
        userAuth.setAuthKey(username);
        userAuth.setAuthSecret(passwordEncoder.encode(request.getPassword()));
        userAuthMapper.insert(userAuth);

        return RegisterResponse.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .auditStatus(USER_AUDIT_PENDING)
            .message("注册成功，账号待管理员审核后可登录")
            .build();
    }

    @Override
    public AuthTokenResponse login(LoginRequest request) {
        String username = normalizeUsername(request.getUsername());
        UserAuthEntity userAuth = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuthEntity>()
            .eq(UserAuthEntity::getAuthType, AUTH_TYPE_PASSWORD)
            .eq(UserAuthEntity::getAuthKey, username)
            .last("limit 1"));

        if (userAuth == null || !passwordEncoder.matches(request.getPassword(), userAuth.getAuthSecret())) {
            throw new BusinessException("学号/工号或密码错误");
        }

        UserEntity user = userMapper.selectById(userAuth.getUserId());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在或已被禁用");
        }
        String auditStatus = user.getAuditStatus();
        if (USER_AUDIT_PENDING.equals(auditStatus)) {
            throw new BusinessException("账号待管理员审核，请稍后再试");
        }
        if (USER_AUDIT_REJECTED.equals(auditStatus)) {
            if (StringUtils.hasText(user.getAuditNote())) {
                throw new BusinessException("账号审核未通过：" + user.getAuditNote().trim());
            }
            throw new BusinessException("账号审核未通过，请联系管理员");
        }
        if (!USER_AUDIT_APPROVED.equals(auditStatus)) {
            throw new BusinessException("账号审核状态异常，请联系管理员");
        }

        return buildTokenResponse(user);
    }

    private boolean existsUsername(String username) {
        Long count = userAuthMapper.selectCount(new LambdaQueryWrapper<UserAuthEntity>()
            .eq(UserAuthEntity::getAuthType, AUTH_TYPE_PASSWORD)
            .eq(UserAuthEntity::getAuthKey, username));
        return count != null && count > 0;
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private AuthTokenResponse buildTokenResponse(UserEntity user) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return AuthTokenResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expireSeconds(jwtProperties.getExpireSeconds())
            .userId(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .build();
    }
}

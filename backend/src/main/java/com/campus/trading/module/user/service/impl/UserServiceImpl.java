package com.campus.trading.module.user.service.impl;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.user.dto.UpdateProfileRequest;
import com.campus.trading.module.user.dto.UserProfileResponse;
import com.campus.trading.module.user.entity.UserEntity;
import com.campus.trading.module.user.mapper.UserMapper;
import com.campus.trading.module.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        UserEntity user = getUserOrThrow(userId);
        return toProfile(user);
    }

    @Override
    public UserProfileResponse updateCurrentUserProfile(Long userId, UpdateProfileRequest request) {
        UserEntity user = getUserOrThrow(userId);
        user.setNickname(request.getNickname().trim());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(normalizeAvatarUrl(request.getAvatarUrl()));
        }
        userMapper.updateById(user);
        return toProfile(user);
    }

    private UserEntity getUserOrThrow(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private UserProfileResponse toProfile(UserEntity user) {
        return UserProfileResponse.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .avatarUrl(normalizeAvatarUrl(user.getAvatarUrl()))
            .status(user.getStatus())
            .auditStatus(user.getAuditStatus())
            .auditNote(user.getAuditNote())
            .auditTime(user.getAuditTime())
            .build();
    }

    private String normalizeAvatarUrl(String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl)) {
            return null;
        }
        String normalized = avatarUrl.trim();
        if ("null".equalsIgnoreCase(normalized)) {
            return null;
        }
        if (normalized.startsWith("/data/uploads/")) {
            return "/uploads/" + normalized.substring("/data/uploads/".length());
        }
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            int index = normalized.indexOf("/data/uploads/");
            if (index > 0) {
                return "/uploads/" + normalized.substring(index + "/data/uploads/".length());
            }
        }
        if (normalized.startsWith("/") || normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized;
        }
        return null;
    }
}

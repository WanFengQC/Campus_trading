package com.campus.trading.module.user.service;

import com.campus.trading.module.user.dto.UpdateProfileRequest;
import com.campus.trading.module.user.dto.UserProfileResponse;

public interface UserService {

    UserProfileResponse getCurrentUserProfile(Long userId);

    UserProfileResponse updateCurrentUserProfile(Long userId, UpdateProfileRequest request);
}

package com.campus.trading.common.security;

import com.campus.trading.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new BusinessException("未登录或登录已失效");
        }
        return authUser;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().userId();
    }
}

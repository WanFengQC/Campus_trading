package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.file.dto.FileUploadResponse;
import com.campus.trading.module.file.service.FileStorageService;
import com.campus.trading.module.user.dto.UpdateProfileRequest;
import com.campus.trading.module.user.dto.UserProfileResponse;
import com.campus.trading.module.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebUserCenterController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public WebUserCenterController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/user/center")
    public String userCenter(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        UserProfileResponse profile = userService.getCurrentUserProfile(userId);
        model.addAttribute("profile", profile);
        return "pages/user-center";
    }

    @PostMapping("/user/profile")
    public String updateProfile(@RequestParam("nickname") String nickname,
                                @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setNickname(nickname);
            request.setAvatarUrl(avatarUrl);
            userService.updateCurrentUserProfile(userId, request);
            redirectAttributes.addFlashAttribute("successMessage", "个人资料已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/user/center";
    }

    @PostMapping("/user/avatar")
    public String uploadAvatar(@RequestParam("file") MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            FileUploadResponse uploadResponse = fileStorageService.uploadAvatar(file, userId);
            UserProfileResponse profile = userService.getCurrentUserProfile(userId);
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setNickname(profile.getNickname());
            request.setAvatarUrl(uploadResponse.getUrl());
            userService.updateCurrentUserProfile(userId, request);
            redirectAttributes.addFlashAttribute("successMessage", "头像已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/user/center";
    }

    private Long getLoginUserId(HttpSession session) {
        Object value = session.getAttribute(WebSessionKeys.LOGIN_USER_ID);
        if (value instanceof Long userId) {
            return userId;
        }
        if (value instanceof Integer userId) {
            return userId.longValue();
        }
        return null;
    }
}

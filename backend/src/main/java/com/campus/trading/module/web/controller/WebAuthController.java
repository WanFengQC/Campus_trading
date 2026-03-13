package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.auth.dto.AuthTokenResponse;
import com.campus.trading.module.auth.dto.LoginRequest;
import com.campus.trading.module.auth.dto.RegisterResponse;
import com.campus.trading.module.auth.dto.RegisterRequest;
import com.campus.trading.module.auth.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebAuthController {

    private final AuthService authService;

    public WebAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "pages/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "pages/register";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            redirectAttributes.addFlashAttribute("errorMessage", "学号/工号和密码不能为空");
            return "redirect:/login";
        }

        try {
            LoginRequest request = new LoginRequest();
            request.setUsername(username);
            request.setPassword(password);
            AuthTokenResponse response = authService.login(request);
            session.setAttribute(WebSessionKeys.LOGIN_USER_ID, response.getUserId());
            return "redirect:/user/center";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam(value = "nickname", required = false) String nickname,
                           RedirectAttributes redirectAttributes) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            redirectAttributes.addFlashAttribute("errorMessage", "学号/工号和密码不能为空");
            return "redirect:/register";
        }

        try {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(username);
            request.setPassword(password);
            request.setNickname(nickname);
            RegisterResponse response = authService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", response.getMessage());
            return "redirect:/login";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(WebSessionKeys.LOGIN_USER_ID);
        session.invalidate();
        return "redirect:/";
    }
}

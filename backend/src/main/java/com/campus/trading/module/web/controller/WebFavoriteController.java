package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.favorite.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebFavoriteController {

    private final FavoriteService favoriteService;

    public WebFavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping("/favorites")
    public String favoritesPage(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("items", favoriteService.listUserFavorites(userId));
        return "pages/favorites";
    }

    @PostMapping("/favorites/toggle")
    public String toggleFavorite(@RequestParam("goodsId") Long goodsId,
                                 @RequestHeader(value = "Referer", required = false) String referer,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录后再操作收藏");
            return "redirect:/login";
        }

        try {
            boolean favorited = favoriteService.toggleFavorite(userId, goodsId);
            if (favorited) {
                redirectAttributes.addFlashAttribute("successMessage", "已加入收藏");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "已取消收藏");
            }
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        if (StringUtils.hasText(referer)) {
            return "redirect:" + referer;
        }
        return "redirect:/goods";
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

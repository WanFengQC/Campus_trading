package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.order.dto.OrderItemResponse;
import com.campus.trading.module.order.service.TradeOrderService;
import com.campus.trading.module.review.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebReviewController {

    private final TradeOrderService tradeOrderService;
    private final ReviewService reviewService;

    public WebReviewController(TradeOrderService tradeOrderService, ReviewService reviewService) {
        this.tradeOrderService = tradeOrderService;
        this.reviewService = reviewService;
    }

    @GetMapping("/orders/{orderId}/review")
    public String reviewPage(@PathVariable("orderId") Long orderId,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        OrderItemResponse order;
        try {
            order = tradeOrderService.getUserOrder(userId, orderId);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "订单不存在或无权访问");
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("existingReview", reviewService.getUserOrderReview(userId, orderId));
        if (!"COMPLETED".equals(order.getStatus())) {
            model.addAttribute("errorMessage", "仅已完成订单可评价");
        }
        model.addAttribute("loggedIn", true);
        return "pages/order-review";
    }

    @PostMapping("/orders/{orderId}/review")
    public String submitReview(@PathVariable("orderId") Long orderId,
                               @RequestParam("score") Integer score,
                               @RequestParam(value = "content", required = false) String content,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            reviewService.createOrderReview(userId, orderId, score, content);
            redirectAttributes.addFlashAttribute("successMessage", "评价提交成功");
            return "redirect:/orders";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/orders/" + orderId + "/review";
        }
    }

    @GetMapping("/reviews/my")
    public String myReviews(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("writtenReviews", reviewService.listUserWrittenReviews(userId));
        model.addAttribute("receivedReviews", reviewService.listUserReceivedReviews(userId));
        model.addAttribute("loggedIn", true);
        return "pages/review-my";
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

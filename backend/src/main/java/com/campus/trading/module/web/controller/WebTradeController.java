package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.cart.dto.CartItemResponse;
import com.campus.trading.module.cart.service.CartService;
import com.campus.trading.module.order.dto.OrderItemResponse;
import com.campus.trading.module.order.dto.OrderLogItemResponse;
import com.campus.trading.module.order.service.TradeOrderService;
import com.campus.trading.module.review.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class WebTradeController {

    private final CartService cartService;
    private final TradeOrderService tradeOrderService;
    private final ReviewService reviewService;

    public WebTradeController(CartService cartService,
                              TradeOrderService tradeOrderService,
                              ReviewService reviewService) {
        this.cartService = cartService;
        this.tradeOrderService = tradeOrderService;
        this.reviewService = reviewService;
    }

    @GetMapping("/cart")
    public String cartPage(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        List<CartItemResponse> items = cartService.listUserCart(userId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemResponse item : items) {
            if (item.getPrice() != null) {
                totalAmount = totalAmount.add(item.getPrice());
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("totalAmount", totalAmount);
        return "pages/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("goodsId") Long goodsId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录后再加入购物车");
            return "redirect:/login";
        }

        try {
            cartService.addItem(userId, goodsId);
            redirectAttributes.addFlashAttribute("successMessage", "商品已加入购物车");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/goods/" + goodsId;
    }

    @PostMapping("/cart/{cartId}/remove")
    public String removeCart(@PathVariable("cartId") Long cartId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            cartService.removeItem(userId, cartId);
            redirectAttributes.addFlashAttribute("successMessage", "已移除购物车商品");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(@RequestParam(value = "buyerRemark", required = false) String buyerRemark,
                           @RequestParam(value = "meetupTime", required = false)
                           @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime meetupTime,
                           @RequestParam(value = "meetupLocation", required = false) String meetupLocation,
                           @RequestParam(value = "meetupNote", required = false) String meetupNote,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            int created = tradeOrderService.checkout(userId, buyerRemark, meetupTime, meetupLocation, meetupNote);
            redirectAttributes.addFlashAttribute("successMessage", "结算成功，已创建 " + created + " 笔订单");
            return "redirect:/orders";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/orders")
    public String ordersPage(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        List<OrderItemResponse> orders = tradeOrderService.listUserOrders(userId);
        List<Long> orderIds = new ArrayList<>();
        for (OrderItemResponse order : orders) {
            orderIds.add(order.getOrderId());
        }
        Set<Long> reviewedOrderIds = reviewService.listReviewedOrderIds(userId, orderIds);
        model.addAttribute("orders", orders);
        model.addAttribute("reviewedOrderIds", reviewedOrderIds);
        return "pages/orders";
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetailPage(@PathVariable("orderId") Long orderId,
                                  Model model,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            OrderItemResponse order = tradeOrderService.getUserOrder(userId, orderId);
            List<OrderLogItemResponse> logs = tradeOrderService.listOrderLogs(userId, orderId);
            boolean reviewed = reviewService.listReviewedOrderIds(userId, List.of(orderId)).contains(orderId);

            model.addAttribute("order", order);
            model.addAttribute("logs", logs);
            model.addAttribute("reviewed", reviewed);
            return "pages/order-detail";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/orders";
        }
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            tradeOrderService.cancelOrder(userId, orderId);
            redirectAttributes.addFlashAttribute("successMessage", "订单已取消");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/orders/{orderId}/seller-confirm")
    public String sellerConfirm(@PathVariable("orderId") Long orderId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            tradeOrderService.sellerConfirm(userId, orderId);
            redirectAttributes.addFlashAttribute("successMessage", "已确认线下交易");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/orders/{orderId}/buyer-complete")
    public String buyerComplete(@PathVariable("orderId") Long orderId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            tradeOrderService.buyerComplete(userId, orderId);
            redirectAttributes.addFlashAttribute("successMessage", "订单已完成");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/orders/{orderId}/meetup")
    public String updateMeetup(@PathVariable("orderId") Long orderId,
                               @RequestParam(value = "meetupTime", required = false)
                               @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime meetupTime,
                               @RequestParam(value = "meetupLocation", required = false) String meetupLocation,
                               @RequestParam(value = "meetupNote", required = false) String meetupNote,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            tradeOrderService.updateMeetup(userId, orderId, meetupTime, meetupLocation, meetupNote);
            redirectAttributes.addFlashAttribute("successMessage", "验货信息已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
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

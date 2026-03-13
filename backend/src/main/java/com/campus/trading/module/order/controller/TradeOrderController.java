package com.campus.trading.module.order.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.security.SecurityUtils;
import com.campus.trading.module.order.dto.OrderItemResponse;
import com.campus.trading.module.order.dto.OrderLogItemResponse;
import com.campus.trading.module.order.service.TradeOrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    public TradeOrderController(TradeOrderService tradeOrderService) {
        this.tradeOrderService = tradeOrderService;
    }

    @GetMapping("/me")
    public ApiResponse<List<OrderItemResponse>> myOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(tradeOrderService.listUserOrders(userId));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderItemResponse> orderDetail(@PathVariable("orderId") Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(tradeOrderService.getUserOrder(userId, orderId));
    }

    @GetMapping("/{orderId}/logs")
    public ApiResponse<List<OrderLogItemResponse>> orderLogs(@PathVariable("orderId") Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(tradeOrderService.listOrderLogs(userId, orderId));
    }

    @PostMapping("/checkout")
    public ApiResponse<Integer> checkout(@RequestParam(value = "buyerRemark", required = false) String buyerRemark,
                                         @RequestParam(value = "meetupTime", required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime meetupTime,
                                         @RequestParam(value = "meetupLocation", required = false) String meetupLocation,
                                         @RequestParam(value = "meetupNote", required = false) String meetupNote) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(tradeOrderService.checkout(userId, buyerRemark, meetupTime, meetupLocation, meetupNote));
    }

    @PostMapping("/{orderId}/meetup")
    public ApiResponse<Void> updateMeetup(@PathVariable("orderId") Long orderId,
                                          @RequestParam(value = "meetupTime", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime meetupTime,
                                          @RequestParam(value = "meetupLocation", required = false) String meetupLocation,
                                          @RequestParam(value = "meetupNote", required = false) String meetupNote) {
        Long userId = SecurityUtils.getCurrentUserId();
        tradeOrderService.updateMeetup(userId, orderId, meetupTime, meetupLocation, meetupNote);
        return ApiResponse.success(null);
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable("orderId") Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        tradeOrderService.cancelOrder(userId, orderId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{orderId}/seller-confirm")
    public ApiResponse<Void> sellerConfirm(@PathVariable("orderId") Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        tradeOrderService.sellerConfirm(userId, orderId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{orderId}/buyer-complete")
    public ApiResponse<Void> buyerComplete(@PathVariable("orderId") Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        tradeOrderService.buyerComplete(userId, orderId);
        return ApiResponse.success(null);
    }
}

package com.campus.trading.module.order.service;

import com.campus.trading.module.order.dto.OrderItemResponse;
import com.campus.trading.module.order.dto.OrderLogItemResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TradeOrderService {

    int checkout(Long buyerId, String buyerRemark, LocalDateTime meetupTime, String meetupLocation, String meetupNote);

    List<OrderItemResponse> listUserOrders(Long userId);

    List<OrderItemResponse> listBuyerOrders(Long buyerId);

    List<OrderItemResponse> listSellerOrders(Long sellerId);

    OrderItemResponse getUserOrder(Long userId, Long orderId);

    List<OrderLogItemResponse> listOrderLogs(Long userId, Long orderId);

    void updateMeetup(Long userId, Long orderId, LocalDateTime meetupTime, String meetupLocation, String meetupNote);

    void cancelOrder(Long buyerId, Long orderId);

    void sellerConfirm(Long sellerId, Long orderId);

    void buyerComplete(Long buyerId, Long orderId);
}

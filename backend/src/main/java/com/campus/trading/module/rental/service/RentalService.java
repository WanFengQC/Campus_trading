package com.campus.trading.module.rental.service;

import com.campus.trading.module.rental.dto.RentalItemResponse;
import com.campus.trading.module.rental.dto.RentalItemSaveRequest;
import com.campus.trading.module.rental.dto.RentalOrderResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RentalService {

    List<RentalItemResponse> searchAvailable(String keyword, Long categoryId, BigDecimal minDailyRent, BigDecimal maxDailyRent);

    RentalItemResponse getDetail(Long itemId);

    RentalItemResponse createItem(Long ownerId, RentalItemSaveRequest request);

    RentalItemResponse updateItem(Long ownerId, Long itemId, RentalItemSaveRequest request);

    void offShelf(Long ownerId, Long itemId);

    List<RentalItemResponse> listOwnerItems(Long ownerId);

    Long createOrder(Long renterId, Long itemId, LocalDate startDate, LocalDate endDate, String renterRemark);

    List<RentalOrderResponse> listUserOrders(Long userId);

    List<RentalOrderResponse> listRenterOrders(Long renterId);

    List<RentalOrderResponse> listOwnerOrders(Long ownerId);

    void cancelOrder(Long renterId, Long orderId);

    void ownerConfirm(Long ownerId, Long orderId);

    void renterComplete(Long renterId, Long orderId);

    void renewOrder(Long renterId, Long orderId, LocalDate newEndDate);
}

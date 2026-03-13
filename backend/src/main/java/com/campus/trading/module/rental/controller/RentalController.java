package com.campus.trading.module.rental.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.security.SecurityUtils;
import com.campus.trading.module.rental.dto.RentalItemResponse;
import com.campus.trading.module.rental.dto.RentalOrderResponse;
import com.campus.trading.module.rental.service.RentalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public ApiResponse<List<RentalItemResponse>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "categoryId", required = false) Long categoryId,
                                                      @RequestParam(value = "minDailyRent", required = false) BigDecimal minDailyRent,
                                                      @RequestParam(value = "maxDailyRent", required = false) BigDecimal maxDailyRent) {
        return ApiResponse.success(rentalService.searchAvailable(keyword, categoryId, minDailyRent, maxDailyRent));
    }

    @GetMapping("/{itemId}")
    public ApiResponse<RentalItemResponse> detail(@PathVariable("itemId") Long itemId) {
        return ApiResponse.success(rentalService.getDetail(itemId));
    }

    @PostMapping("/{itemId}/order")
    public ApiResponse<Long> createOrder(@PathVariable("itemId") Long itemId,
                                         @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                         @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                         @RequestParam(value = "renterRemark", required = false) String renterRemark) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(rentalService.createOrder(userId, itemId, startDate, endDate, renterRemark));
    }

    @GetMapping("/orders/me")
    public ApiResponse<List<RentalOrderResponse>> myOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(rentalService.listUserOrders(userId));
    }
}

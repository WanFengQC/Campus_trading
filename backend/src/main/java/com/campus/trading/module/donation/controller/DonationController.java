package com.campus.trading.module.donation.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.security.SecurityUtils;
import com.campus.trading.module.donation.dto.DonationItemResponse;
import com.campus.trading.module.donation.dto.DonationItemSaveRequest;
import com.campus.trading.module.donation.dto.DonationRecordResponse;
import com.campus.trading.module.donation.service.DonationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @GetMapping
    public ApiResponse<List<DonationItemResponse>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "categoryId", required = false) Long categoryId) {
        return ApiResponse.success(donationService.searchAvailable(keyword, categoryId));
    }

    @GetMapping("/{itemId}")
    public ApiResponse<DonationItemResponse> detail(@PathVariable("itemId") Long itemId) {
        return ApiResponse.success(donationService.getDetail(itemId));
    }

    @PostMapping
    public ApiResponse<DonationItemResponse> create(@RequestBody DonationItemSaveRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(donationService.createItem(userId, request));
    }

    @GetMapping("/mine")
    public ApiResponse<List<DonationItemResponse>> mine() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(donationService.listDonorItems(userId));
    }

    @PostMapping("/{itemId}/off-shelf")
    public ApiResponse<Void> offShelf(@PathVariable("itemId") Long itemId) {
        Long userId = SecurityUtils.getCurrentUserId();
        donationService.offShelf(userId, itemId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{itemId}/claim")
    public ApiResponse<Long> claim(@PathVariable("itemId") Long itemId,
                                   @RequestParam(value = "claimRemark", required = false) String claimRemark) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(donationService.claim(userId, itemId, claimRemark));
    }

    @GetMapping("/records/me")
    public ApiResponse<List<DonationRecordResponse>> records() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(donationService.listUserRecords(userId));
    }

    @PostMapping("/records/{recordId}/cancel")
    public ApiResponse<Void> cancelClaim(@PathVariable("recordId") Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        donationService.cancelClaim(userId, recordId);
        return ApiResponse.success(null);
    }

    @PostMapping("/records/{recordId}/approve")
    public ApiResponse<Void> approveClaim(@PathVariable("recordId") Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        donationService.approveClaim(userId, recordId);
        return ApiResponse.success(null);
    }

    @PostMapping("/records/{recordId}/reject")
    public ApiResponse<Void> rejectClaim(@PathVariable("recordId") Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        donationService.rejectClaim(userId, recordId);
        return ApiResponse.success(null);
    }

    @PostMapping("/records/{recordId}/complete")
    public ApiResponse<Void> completeClaim(@PathVariable("recordId") Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        donationService.completeClaim(userId, recordId);
        return ApiResponse.success(null);
    }
}

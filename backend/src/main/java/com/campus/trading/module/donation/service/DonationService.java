package com.campus.trading.module.donation.service;

import com.campus.trading.module.donation.dto.DonationItemResponse;
import com.campus.trading.module.donation.dto.DonationItemSaveRequest;
import com.campus.trading.module.donation.dto.DonationRecordResponse;

import java.util.List;

public interface DonationService {

    List<DonationItemResponse> searchAvailable(String keyword, Long categoryId);

    DonationItemResponse getDetail(Long itemId);

    DonationItemResponse createItem(Long donorId, DonationItemSaveRequest request);

    void offShelf(Long donorId, Long itemId);

    List<DonationItemResponse> listDonorItems(Long donorId);

    Long claim(Long claimerId, Long itemId, String claimRemark);

    List<DonationRecordResponse> listUserRecords(Long userId);

    List<DonationRecordResponse> listClaimerRecords(Long claimerId);

    List<DonationRecordResponse> listDonorRecords(Long donorId);

    void cancelClaim(Long claimerId, Long recordId);

    void approveClaim(Long donorId, Long recordId);

    void rejectClaim(Long donorId, Long recordId);

    void completeClaim(Long claimerId, Long recordId);
}

package com.campus.trading.module.browse.service;

import com.campus.trading.module.browse.dto.BrowseHistoryItemResponse;

import java.util.List;

public interface BrowseHistoryService {

    void recordView(Long userId, Long goodsId);

    List<BrowseHistoryItemResponse> listUserHistory(Long userId);
}

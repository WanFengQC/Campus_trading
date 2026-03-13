package com.campus.trading.module.browse.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.security.SecurityUtils;
import com.campus.trading.module.browse.dto.BrowseHistoryItemResponse;
import com.campus.trading.module.browse.service.BrowseHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/browse")
public class BrowseHistoryController {

    private final BrowseHistoryService browseHistoryService;

    public BrowseHistoryController(BrowseHistoryService browseHistoryService) {
        this.browseHistoryService = browseHistoryService;
    }

    @GetMapping("/me")
    public ApiResponse<List<BrowseHistoryItemResponse>> me() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(browseHistoryService.listUserHistory(userId));
    }
}

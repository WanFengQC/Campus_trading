package com.campus.trading.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardResponse {

    private Long totalUserCount;

    private Long onShelfGoodsCount;

    private Long pendingOrderCount;

    private Long activeRentalCount;

    private Long availableDonationCount;
}

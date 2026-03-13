package com.campus.trading.module.report.service;

import com.campus.trading.module.report.dto.ReportResponse;

import java.util.List;

public interface ReportService {

    Long createReport(Long reporterId, String targetType, Long targetId, String reason, String detail);

    List<ReportResponse> listUserReports(Long reporterId);

    List<ReportResponse> listReports(String status, String targetType);

    void processReport(Long reportId, String status, String handleNote);
}

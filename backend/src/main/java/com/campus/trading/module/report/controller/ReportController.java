package com.campus.trading.module.report.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.security.SecurityUtils;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.report.dto.ReportResponse;
import com.campus.trading.module.report.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestParam("targetType") String targetType,
                                    @RequestParam("targetId") Long targetId,
                                    @RequestParam("reason") String reason,
                                    @RequestParam(value = "detail", required = false) String detail) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(reportService.createReport(userId, targetType, targetId, reason, detail));
    }

    @GetMapping("/me")
    public ApiResponse<List<ReportResponse>> myReports() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(reportService.listUserReports(userId));
    }

    @GetMapping
    public ApiResponse<List<ReportResponse>> list(@RequestParam(value = "status", required = false) String status,
                                                  @RequestParam(value = "targetType", required = false) String targetType,
                                                  HttpSession session) {
        Object adminId = session.getAttribute(WebSessionKeys.ADMIN_USER_ID);
        if (!(adminId instanceof Long) && !(adminId instanceof Integer)) {
            throw new BusinessException("未登录管理员账号");
        }
        return ApiResponse.success(reportService.listReports(status, targetType));
    }

    @PostMapping("/{reportId}/process")
    public ApiResponse<Void> process(@PathVariable("reportId") Long reportId,
                                     @RequestParam("status") String status,
                                     @RequestParam(value = "handleNote", required = false) String handleNote,
                                     HttpSession session) {
        Object adminId = session.getAttribute(WebSessionKeys.ADMIN_USER_ID);
        if (!(adminId instanceof Long) && !(adminId instanceof Integer)) {
            throw new BusinessException("未登录管理员账号");
        }
        reportService.processReport(reportId, status, handleNote);
        return ApiResponse.success(null);
    }
}

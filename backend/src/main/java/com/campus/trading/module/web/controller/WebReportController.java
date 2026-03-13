package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.report.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebReportController {

    private final ReportService reportService;

    public WebReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports/create")
    public String createPage(@RequestParam(value = "targetType", required = false) String targetType,
                             @RequestParam(value = "targetId", required = false) Long targetId,
                             Model model,
                             HttpSession session) {
        if (getLoginUserId(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetId", targetId);
        return "pages/report-create";
    }

    @PostMapping("/reports/create")
    public String create(@RequestParam("targetType") String targetType,
                         @RequestParam("targetId") Long targetId,
                         @RequestParam("reason") String reason,
                         @RequestParam(value = "detail", required = false) String detail,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            reportService.createReport(userId, targetType, targetId, reason, detail);
            redirectAttributes.addFlashAttribute("successMessage", "举报已提交，我们会尽快处理");
            return "redirect:/reports/my";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/reports/create?targetType=" + targetType + "&targetId=" + targetId;
        }
    }

    @GetMapping("/reports/my")
    public String myReports(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("items", reportService.listUserReports(userId));
        return "pages/report-my";
    }

    @PostMapping("/admin/reports/{reportId}/process")
    public String process(@PathVariable("reportId") Long reportId,
                          @RequestParam("status") String status,
                          @RequestParam(value = "handleNote", required = false) String handleNote,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (getAdminId(session) == null) {
            return "redirect:/admin/login";
        }
        try {
            reportService.processReport(reportId, status, handleNote);
            redirectAttributes.addFlashAttribute("successMessage", "举报处理成功");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/reports";
    }

    private Long getLoginUserId(HttpSession session) {
        Object value = session.getAttribute(WebSessionKeys.LOGIN_USER_ID);
        if (value instanceof Long userId) {
            return userId;
        }
        if (value instanceof Integer userId) {
            return userId.longValue();
        }
        return null;
    }

    private Long getAdminId(HttpSession session) {
        Object value = session.getAttribute(WebSessionKeys.ADMIN_USER_ID);
        if (value instanceof Long adminId) {
            return adminId;
        }
        if (value instanceof Integer adminId) {
            return adminId.longValue();
        }
        return null;
    }
}

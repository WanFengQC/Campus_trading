package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.admin.dto.AdminLoginResponse;
import com.campus.trading.module.admin.service.AdminService;
import com.campus.trading.module.notice.service.NoticeService;
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
public class WebAdminController {

    private final AdminService adminService;
    private final ReportService reportService;
    private final NoticeService noticeService;

    public WebAdminController(AdminService adminService,
                              ReportService reportService,
                              NoticeService noticeService) {
        this.adminService = adminService;
        this.reportService = reportService;
        this.noticeService = noticeService;
    }

    @GetMapping("/admin/login")
    public String loginPage(HttpSession session) {
        if (getAdminId(session) != null) {
            return "redirect:/admin";
        }
        return "pages/admin-login";
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            AdminLoginResponse admin = adminService.login(username, password);
            session.setAttribute(WebSessionKeys.ADMIN_USER_ID, admin.getAdminId());
            session.setAttribute(WebSessionKeys.ADMIN_USERNAME, admin.getNickname());
            return "redirect:/admin";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/login";
        }
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(WebSessionKeys.ADMIN_USER_ID);
        session.removeAttribute(WebSessionKeys.ADMIN_USERNAME);
        return "redirect:/admin/login";
    }

    @GetMapping("/admin")
    public String dashboard(Model model, HttpSession session) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("dashboard", adminService.getDashboard());
        applyCommon(model, session);
        return "pages/admin-dashboard";
    }

    @GetMapping("/admin/users")
    public String users(@RequestParam(value = "keyword", required = false) String keyword,
                        Model model,
                        HttpSession session) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("users", adminService.listUsers(keyword));
        model.addAttribute("keyword", keyword);
        applyCommon(model, session);
        return "pages/admin-users";
    }

    @PostMapping("/admin/users/{userId}/status")
    public String updateUserStatus(@PathVariable("userId") Long userId,
                                   @RequestParam("status") Integer status,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            adminService.updateUserStatus(userId, status);
            redirectAttributes.addFlashAttribute("successMessage", "用户状态已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{userId}/audit")
    public String auditUser(@PathVariable("userId") Long userId,
                            @RequestParam("auditStatus") String auditStatus,
                            @RequestParam(value = "auditNote", required = false) String auditNote,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            adminService.auditUser(userId, auditStatus, auditNote);
            redirectAttributes.addFlashAttribute("successMessage", "用户审核状态已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/categories")
    public String categories(Model model, HttpSession session) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("categories", adminService.listCategories());
        applyCommon(model, session);
        return "pages/admin-categories";
    }

    @PostMapping("/admin/categories/create")
    public String createCategory(@RequestParam("name") String name,
                                 @RequestParam(value = "sortNo", required = false) Integer sortNo,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            adminService.createCategory(name, sortNo);
            redirectAttributes.addFlashAttribute("successMessage", "分类创建成功");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/{categoryId}/update")
    public String updateCategory(@PathVariable("categoryId") Long categoryId,
                                 @RequestParam("name") String name,
                                 @RequestParam("sortNo") Integer sortNo,
                                 @RequestParam("status") Integer status,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            adminService.updateCategory(categoryId, name, sortNo, status);
            redirectAttributes.addFlashAttribute("successMessage", "分类已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/goods")
    public String goods(@RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "auditStatus", required = false) String auditStatus,
                        Model model,
                        HttpSession session) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("items", adminService.listGoods(keyword, status, auditStatus));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("auditStatus", auditStatus);
        applyCommon(model, session);
        return "pages/admin-goods";
    }

    @PostMapping("/admin/goods/{goodsId}/off-shelf")
    public String offShelfGoods(@PathVariable("goodsId") Long goodsId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            adminService.offShelfGoods(goodsId);
            redirectAttributes.addFlashAttribute("successMessage", "商品已下架");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/goods";
    }

    @PostMapping("/admin/goods/{goodsId}/audit")
    public String auditGoods(@PathVariable("goodsId") Long goodsId,
                             @RequestParam("auditStatus") String auditStatus,
                             @RequestParam(value = "auditNote", required = false) String auditNote,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            adminService.auditGoods(goodsId, auditStatus, auditNote);
            redirectAttributes.addFlashAttribute("successMessage", "商品审核状态已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/goods";
    }

    @GetMapping("/admin/orders")
    public String orders(@RequestParam(value = "status", required = false) String status,
                         Model model,
                         HttpSession session) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("orders", adminService.listOrders(status));
        model.addAttribute("status", status);
        applyCommon(model, session);
        return "pages/admin-orders";
    }

    @GetMapping("/admin/rentals")
    public String rentals(@RequestParam(value = "status", required = false) String status,
                          Model model,
                          HttpSession session) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("orders", adminService.listRentalOrders(status));
        model.addAttribute("status", status);
        applyCommon(model, session);
        return "pages/admin-rentals";
    }

    @PostMapping("/admin/rentals/{orderId}/status")
    public String updateRentalStatus(@PathVariable("orderId") Long orderId,
                                     @RequestParam("status") String status,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            adminService.updateRentalOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("successMessage", "租赁订单状态已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/rentals";
    }

    @GetMapping("/admin/donations")
    public String donations(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "itemStatus", required = false) String itemStatus,
                            @RequestParam(value = "recordStatus", required = false) String recordStatus,
                            Model model,
                            HttpSession session) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("items", adminService.listDonationItems(keyword, itemStatus));
        model.addAttribute("records", adminService.listDonationRecords(recordStatus));
        model.addAttribute("keyword", keyword);
        model.addAttribute("itemStatus", itemStatus);
        model.addAttribute("recordStatus", recordStatus);
        applyCommon(model, session);
        return "pages/admin-donations";
    }

    @PostMapping("/admin/donations/items/{itemId}/status")
    public String updateDonationItemStatus(@PathVariable("itemId") Long itemId,
                                           @RequestParam("status") String status,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            adminService.updateDonationItemStatus(itemId, status);
            redirectAttributes.addFlashAttribute("successMessage", "捐赠物品状态已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/donations";
    }

    @PostMapping("/admin/donations/records/{recordId}/status")
    public String updateDonationRecordStatus(@PathVariable("recordId") Long recordId,
                                             @RequestParam("status") String status,
                                             HttpSession session,
                                             RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            adminService.updateDonationRecordStatus(recordId, status);
            redirectAttributes.addFlashAttribute("successMessage", "认领记录状态已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/donations";
    }

    @GetMapping("/admin/reports")
    public String reports(@RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "targetType", required = false) String targetType,
                          Model model,
                          HttpSession session) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("items", reportService.listReports(status, targetType));
        model.addAttribute("status", status);
        model.addAttribute("targetType", targetType);
        applyCommon(model, session);
        return "pages/admin-reports";
    }

    @GetMapping("/admin/notices")
    public String notices(@RequestParam(value = "status", required = false) Integer status,
                          Model model,
                          HttpSession session) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("items", noticeService.listAdmin(status));
        model.addAttribute("status", status);
        applyCommon(model, session);
        return "pages/admin-notices";
    }

    @PostMapping("/admin/notices/create")
    public String createNotice(@RequestParam("title") String title,
                               @RequestParam("content") String content,
                               @RequestParam(value = "sortNo", required = false) Integer sortNo,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            Object nameObj = session.getAttribute(WebSessionKeys.ADMIN_USERNAME);
            String adminName = nameObj == null ? "管理员" : nameObj.toString();
            noticeService.createNotice(title, content, sortNo, adminName);
            redirectAttributes.addFlashAttribute("successMessage", "公告创建成功");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/notices";
    }

    @PostMapping("/admin/notices/{noticeId}/update")
    public String updateNotice(@PathVariable("noticeId") Long noticeId,
                               @RequestParam("title") String title,
                               @RequestParam("content") String content,
                               @RequestParam("status") Integer status,
                               @RequestParam("sortNo") Integer sortNo,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            noticeService.updateNotice(noticeId, title, content, status, sortNo);
            redirectAttributes.addFlashAttribute("successMessage", "公告更新成功");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/notices";
    }

    @PostMapping("/admin/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable("orderId") Long orderId,
                                    @RequestParam("status") String status,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!ensureAdminLogin(session)) {
            return "redirect:/admin/login";
        }
        try {
            adminService.updateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("successMessage", "订单状态已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/orders";
    }

    private boolean ensureAdminLogin(HttpSession session) {
        return getAdminId(session) != null;
    }

    private void applyCommon(Model model, HttpSession session) {
        Object adminName = session.getAttribute(WebSessionKeys.ADMIN_USERNAME);
        model.addAttribute("adminName", adminName == null ? "管理员" : adminName.toString());
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

package com.campus.trading.module.admin.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.admin.dto.AdminCategoryManageItemResponse;
import com.campus.trading.module.admin.dto.AdminDashboardResponse;
import com.campus.trading.module.admin.dto.AdminDonationItemManageItemResponse;
import com.campus.trading.module.admin.dto.AdminDonationRecordManageItemResponse;
import com.campus.trading.module.admin.dto.AdminGoodsManageItemResponse;
import com.campus.trading.module.admin.dto.AdminOrderManageItemResponse;
import com.campus.trading.module.admin.dto.AdminRentalOrderManageItemResponse;
import com.campus.trading.module.admin.dto.AdminUserManageItemResponse;
import com.campus.trading.module.admin.service.AdminService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> dashboard(HttpSession session) {
        ensureAdminLogin(session);
        return ApiResponse.success(adminService.getDashboard());
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserManageItemResponse>> users(@RequestParam(value = "keyword", required = false) String keyword,
                                                                HttpSession session) {
        ensureAdminLogin(session);
        return ApiResponse.success(adminService.listUsers(keyword));
    }

    @PostMapping("/users/{userId}/status")
    public ApiResponse<Void> userStatus(@PathVariable("userId") Long userId,
                                        @RequestParam("status") Integer status,
                                        HttpSession session) {
        ensureAdminLogin(session);
        adminService.updateUserStatus(userId, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/users/{userId}/audit")
    public ApiResponse<Void> userAudit(@PathVariable("userId") Long userId,
                                       @RequestParam("auditStatus") String auditStatus,
                                       @RequestParam(value = "auditNote", required = false) String auditNote,
                                       HttpSession session) {
        ensureAdminLogin(session);
        adminService.auditUser(userId, auditStatus, auditNote);
        return ApiResponse.success(null);
    }

    @GetMapping("/categories")
    public ApiResponse<List<AdminCategoryManageItemResponse>> categories(HttpSession session) {
        ensureAdminLogin(session);
        return ApiResponse.success(adminService.listCategories());
    }

    @GetMapping("/goods")
    public ApiResponse<List<AdminGoodsManageItemResponse>> goods(@RequestParam(value = "keyword", required = false) String keyword,
                                                                 @RequestParam(value = "status", required = false) String status,
                                                                 @RequestParam(value = "auditStatus", required = false) String auditStatus,
                                                                 HttpSession session) {
        ensureAdminLogin(session);
        return ApiResponse.success(adminService.listGoods(keyword, status, auditStatus));
    }

    @PostMapping("/goods/{goodsId}/audit")
    public ApiResponse<Void> goodsAudit(@PathVariable("goodsId") Long goodsId,
                                        @RequestParam("auditStatus") String auditStatus,
                                        @RequestParam(value = "auditNote", required = false) String auditNote,
                                        HttpSession session) {
        ensureAdminLogin(session);
        adminService.auditGoods(goodsId, auditStatus, auditNote);
        return ApiResponse.success(null);
    }

    @GetMapping("/orders")
    public ApiResponse<List<AdminOrderManageItemResponse>> orders(@RequestParam(value = "status", required = false) String status,
                                                                  HttpSession session) {
        ensureAdminLogin(session);
        return ApiResponse.success(adminService.listOrders(status));
    }

    @GetMapping("/rentals")
    public ApiResponse<List<AdminRentalOrderManageItemResponse>> rentals(@RequestParam(value = "status", required = false) String status,
                                                                         HttpSession session) {
        ensureAdminLogin(session);
        return ApiResponse.success(adminService.listRentalOrders(status));
    }

    @PostMapping("/rentals/{orderId}/status")
    public ApiResponse<Void> rentalStatus(@PathVariable("orderId") Long orderId,
                                          @RequestParam("status") String status,
                                          HttpSession session) {
        ensureAdminLogin(session);
        adminService.updateRentalOrderStatus(orderId, status);
        return ApiResponse.success(null);
    }

    @GetMapping("/donations/items")
    public ApiResponse<List<AdminDonationItemManageItemResponse>> donationItems(@RequestParam(value = "keyword", required = false) String keyword,
                                                                                 @RequestParam(value = "status", required = false) String status,
                                                                                 HttpSession session) {
        ensureAdminLogin(session);
        return ApiResponse.success(adminService.listDonationItems(keyword, status));
    }

    @PostMapping("/donations/items/{itemId}/status")
    public ApiResponse<Void> donationItemStatus(@PathVariable("itemId") Long itemId,
                                                @RequestParam("status") String status,
                                                HttpSession session) {
        ensureAdminLogin(session);
        adminService.updateDonationItemStatus(itemId, status);
        return ApiResponse.success(null);
    }

    @GetMapping("/donations/records")
    public ApiResponse<List<AdminDonationRecordManageItemResponse>> donationRecords(@RequestParam(value = "status", required = false) String status,
                                                                                     HttpSession session) {
        ensureAdminLogin(session);
        return ApiResponse.success(adminService.listDonationRecords(status));
    }

    @PostMapping("/donations/records/{recordId}/status")
    public ApiResponse<Void> donationRecordStatus(@PathVariable("recordId") Long recordId,
                                                  @RequestParam("status") String status,
                                                  HttpSession session) {
        ensureAdminLogin(session);
        adminService.updateDonationRecordStatus(recordId, status);
        return ApiResponse.success(null);
    }

    private void ensureAdminLogin(HttpSession session) {
        if (session == null) {
            throw new BusinessException("未登录管理员账号");
        }
        Object adminId = session.getAttribute(WebSessionKeys.ADMIN_USER_ID);
        if (!(adminId instanceof Long) && !(adminId instanceof Integer)) {
            throw new BusinessException("未登录管理员账号");
        }
    }
}

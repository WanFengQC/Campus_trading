package com.campus.trading.module.admin.service;

import com.campus.trading.module.admin.dto.AdminCategoryManageItemResponse;
import com.campus.trading.module.admin.dto.AdminDashboardResponse;
import com.campus.trading.module.admin.dto.AdminDonationItemManageItemResponse;
import com.campus.trading.module.admin.dto.AdminDonationRecordManageItemResponse;
import com.campus.trading.module.admin.dto.AdminGoodsManageItemResponse;
import com.campus.trading.module.admin.dto.AdminLoginResponse;
import com.campus.trading.module.admin.dto.AdminOrderManageItemResponse;
import com.campus.trading.module.admin.dto.AdminRentalOrderManageItemResponse;
import com.campus.trading.module.admin.dto.AdminUserManageItemResponse;

import java.util.List;

public interface AdminService {

    AdminLoginResponse login(String username, String password);

    AdminDashboardResponse getDashboard();

    List<AdminUserManageItemResponse> listUsers(String keyword);

    void updateUserStatus(Long userId, Integer status);

    void auditUser(Long userId, String auditStatus, String auditNote);

    List<AdminCategoryManageItemResponse> listCategories();

    void createCategory(String name, Integer sortNo);

    void updateCategory(Long categoryId, String name, Integer sortNo, Integer status);

    List<AdminGoodsManageItemResponse> listGoods(String keyword, String status, String auditStatus);

    void offShelfGoods(Long goodsId);

    void auditGoods(Long goodsId, String auditStatus, String auditNote);

    List<AdminOrderManageItemResponse> listOrders(String status);

    void updateOrderStatus(Long orderId, String status);

    List<AdminRentalOrderManageItemResponse> listRentalOrders(String status);

    void updateRentalOrderStatus(Long orderId, String status);

    List<AdminDonationItemManageItemResponse> listDonationItems(String keyword, String status);

    void updateDonationItemStatus(Long itemId, String status);

    List<AdminDonationRecordManageItemResponse> listDonationRecords(String status);

    void updateDonationRecordStatus(Long recordId, String status);
}

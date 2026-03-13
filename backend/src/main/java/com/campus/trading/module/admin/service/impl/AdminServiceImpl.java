package com.campus.trading.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.admin.dto.AdminCategoryManageItemResponse;
import com.campus.trading.module.admin.dto.AdminDashboardResponse;
import com.campus.trading.module.admin.dto.AdminDonationItemManageItemResponse;
import com.campus.trading.module.admin.dto.AdminDonationRecordManageItemResponse;
import com.campus.trading.module.admin.dto.AdminGoodsManageItemResponse;
import com.campus.trading.module.admin.dto.AdminLoginResponse;
import com.campus.trading.module.admin.dto.AdminOrderManageItemResponse;
import com.campus.trading.module.admin.dto.AdminRentalOrderManageItemResponse;
import com.campus.trading.module.admin.dto.AdminUserManageItemResponse;
import com.campus.trading.module.admin.entity.AdminUserEntity;
import com.campus.trading.module.admin.mapper.AdminUserMapper;
import com.campus.trading.module.admin.service.AdminService;
import com.campus.trading.module.category.entity.CategoryEntity;
import com.campus.trading.module.category.mapper.CategoryMapper;
import com.campus.trading.module.donation.entity.DonationItemEntity;
import com.campus.trading.module.donation.entity.DonationRecordEntity;
import com.campus.trading.module.donation.mapper.DonationItemMapper;
import com.campus.trading.module.donation.mapper.DonationRecordMapper;
import com.campus.trading.module.goods.entity.GoodsEntity;
import com.campus.trading.module.goods.mapper.GoodsMapper;
import com.campus.trading.module.order.entity.TradeOrderEntity;
import com.campus.trading.module.order.mapper.TradeOrderMapper;
import com.campus.trading.module.rental.entity.RentalItemEntity;
import com.campus.trading.module.rental.entity.RentalOrderEntity;
import com.campus.trading.module.rental.mapper.RentalItemMapper;
import com.campus.trading.module.rental.mapper.RentalOrderMapper;
import com.campus.trading.module.user.entity.UserEntity;
import com.campus.trading.module.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD_HASH = "$2a$10$x3jRkgE2sGeWjZrHUIbXgOmOLm//eCVjIG/wrfO5Sa4C1DWevwPL2";
    private static final String GOODS_STATUS_ON_SHELF = "ON_SHELF";
    private static final String GOODS_STATUS_OFF_SHELF = "OFF_SHELF";
    private static final String GOODS_AUDIT_PENDING = "PENDING";
    private static final String GOODS_AUDIT_APPROVED = "APPROVED";
    private static final String GOODS_AUDIT_REJECTED = "REJECTED";
    private static final String ORDER_STATUS_PENDING = "PENDING";
    private static final String ORDER_STATUS_SELLER_CONFIRMED = "SELLER_CONFIRMED";
    private static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final String USER_AUDIT_PENDING = "PENDING";
    private static final String USER_AUDIT_APPROVED = "APPROVED";
    private static final String USER_AUDIT_REJECTED = "REJECTED";

    private static final String RENTAL_ORDER_STATUS_PENDING = "PENDING";
    private static final String RENTAL_ORDER_STATUS_ACTIVE = "ACTIVE";
    private static final String RENTAL_ORDER_STATUS_COMPLETED = "COMPLETED";
    private static final String RENTAL_ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final String RENTAL_ITEM_STATUS_AVAILABLE = "AVAILABLE";
    private static final String RENTAL_ITEM_STATUS_RENTING = "RENTING";

    private static final String DONATION_ITEM_STATUS_AVAILABLE = "AVAILABLE";
    private static final String DONATION_ITEM_STATUS_CLAIM_PENDING = "CLAIM_PENDING";
    private static final String DONATION_ITEM_STATUS_CLAIMED = "CLAIMED";
    private static final String DONATION_ITEM_STATUS_OFF_SHELF = "OFF_SHELF";
    private static final String DONATION_RECORD_STATUS_PENDING = "PENDING";
    private static final String DONATION_RECORD_STATUS_APPROVED = "APPROVED";
    private static final String DONATION_RECORD_STATUS_REJECTED = "REJECTED";
    private static final String DONATION_RECORD_STATUS_CANCELLED = "CANCELLED";
    private static final String DONATION_RECORD_STATUS_COMPLETED = "COMPLETED";
    private static final String RENTAL_STATUS_ACTIVE = "ACTIVE";
    private static final String DONATION_STATUS_AVAILABLE = "AVAILABLE";

    private final AdminUserMapper adminUserMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final GoodsMapper goodsMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final RentalItemMapper rentalItemMapper;
    private final RentalOrderMapper rentalOrderMapper;
    private final DonationItemMapper donationItemMapper;
    private final DonationRecordMapper donationRecordMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(AdminUserMapper adminUserMapper,
                            UserMapper userMapper,
                            CategoryMapper categoryMapper,
                            GoodsMapper goodsMapper,
                            TradeOrderMapper tradeOrderMapper,
                            RentalItemMapper rentalItemMapper,
                            RentalOrderMapper rentalOrderMapper,
                            DonationItemMapper donationItemMapper,
                            DonationRecordMapper donationRecordMapper,
                            PasswordEncoder passwordEncoder) {
        this.adminUserMapper = adminUserMapper;
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.goodsMapper = goodsMapper;
        this.tradeOrderMapper = tradeOrderMapper;
        this.rentalItemMapper = rentalItemMapper;
        this.rentalOrderMapper = rentalOrderMapper;
        this.donationItemMapper = donationItemMapper;
        this.donationRecordMapper = donationRecordMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdminLoginResponse login(String username, String password) {
        ensureDefaultAdminUser();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException("管理员账号或密码不能为空");
        }

        AdminUserEntity admin = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUserEntity>()
            .eq(AdminUserEntity::getUsername, username.trim())
            .last("limit 1"));
        if (admin == null || admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BusinessException("管理员账号或密码错误");
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException("管理员账号或密码错误");
        }
        return AdminLoginResponse.builder()
            .adminId(admin.getId())
            .username(admin.getUsername())
            .nickname(StringUtils.hasText(admin.getNickname()) ? admin.getNickname().trim() : admin.getUsername())
            .build();
    }

    @Override
    public AdminDashboardResponse getDashboard() {
        ensureDefaultAdminUser();
        Long totalUsers = userMapper.selectCount(null);
        Long onShelfGoods = goodsMapper.selectCount(new LambdaQueryWrapper<GoodsEntity>()
            .eq(GoodsEntity::getStatus, GOODS_STATUS_ON_SHELF)
            .eq(GoodsEntity::getAuditStatus, GOODS_AUDIT_APPROVED));
        Long pendingOrders = tradeOrderMapper.selectCount(new LambdaQueryWrapper<TradeOrderEntity>()
            .eq(TradeOrderEntity::getStatus, ORDER_STATUS_PENDING));
        Long activeRentals = rentalOrderMapper.selectCount(new LambdaQueryWrapper<RentalOrderEntity>()
            .eq(RentalOrderEntity::getStatus, RENTAL_STATUS_ACTIVE));
        Long availableDonations = donationItemMapper.selectCount(new LambdaQueryWrapper<DonationItemEntity>()
            .eq(DonationItemEntity::getStatus, DONATION_STATUS_AVAILABLE));

        return AdminDashboardResponse.builder()
            .totalUserCount(orZero(totalUsers))
            .onShelfGoodsCount(orZero(onShelfGoods))
            .pendingOrderCount(orZero(pendingOrders))
            .activeRentalCount(orZero(activeRentals))
            .availableDonationCount(orZero(availableDonations))
            .build();
    }

    @Override
    public List<AdminUserManageItemResponse> listUsers(String keyword) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>()
            .orderByDesc(UserEntity::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            wrapper.and(w -> w.like(UserEntity::getUsername, trimmed).or().like(UserEntity::getNickname, trimmed));
        }
        List<UserEntity> users = userMapper.selectList(wrapper);
        List<AdminUserManageItemResponse> result = new ArrayList<>();
        for (UserEntity user : users) {
            result.add(AdminUserManageItemResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .status(user.getStatus())
                .auditStatus(user.getAuditStatus())
                .auditStatusLabel(resolveUserAuditStatusLabel(user.getAuditStatus()))
                .auditNote(user.getAuditNote())
                .auditTime(user.getAuditTime())
                .createdAt(user.getCreatedAt())
                .build());
        }
        return result;
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("用户状态只支持 0 或 1");
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void auditUser(Long userId, String auditStatus, String auditNote) {
        String targetStatus = auditStatus == null ? "" : auditStatus.trim();
        if (!USER_AUDIT_APPROVED.equals(targetStatus) && !USER_AUDIT_REJECTED.equals(targetStatus)) {
            throw new BusinessException("用户审核状态仅支持通过或驳回");
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setAuditStatus(targetStatus);
        user.setAuditNote(sanitizeAuditNote(auditNote));
        user.setAuditTime(LocalDateTime.now());
        if (USER_AUDIT_APPROVED.equals(targetStatus) && (user.getStatus() == null || user.getStatus() != 1)) {
            user.setStatus(1);
        }
        userMapper.updateById(user);
    }

    @Override
    public List<AdminCategoryManageItemResponse> listCategories() {
        List<CategoryEntity> categories = categoryMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
            .orderByAsc(CategoryEntity::getSortNo)
            .orderByAsc(CategoryEntity::getId));
        List<AdminCategoryManageItemResponse> result = new ArrayList<>();
        for (CategoryEntity category : categories) {
            result.add(AdminCategoryManageItemResponse.builder()
                .categoryId(category.getId())
                .name(category.getName())
                .sortNo(category.getSortNo())
                .status(category.getStatus())
                .build());
        }
        return result;
    }

    @Override
    public void createCategory(String name, Integer sortNo) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException("分类名称不能为空");
        }
        CategoryEntity category = new CategoryEntity();
        category.setName(limit(name.trim(), 64));
        category.setSortNo(sortNo == null ? 0 : sortNo);
        category.setStatus(1);
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long categoryId, String name, Integer sortNo, Integer status) {
        CategoryEntity category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (StringUtils.hasText(name)) {
            category.setName(limit(name.trim(), 64));
        }
        if (sortNo != null) {
            category.setSortNo(sortNo);
        }
        if (status != null) {
            if (status != 0 && status != 1) {
                throw new BusinessException("分类状态只支持 0 或 1");
            }
            category.setStatus(status);
        }
        categoryMapper.updateById(category);
    }

    @Override
    public List<AdminGoodsManageItemResponse> listGoods(String keyword, String status, String auditStatus) {
        LambdaQueryWrapper<GoodsEntity> wrapper = new LambdaQueryWrapper<GoodsEntity>()
            .orderByDesc(GoodsEntity::getCreatedAt);

        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            wrapper.and(w -> w.like(GoodsEntity::getTitle, trimmed).or().like(GoodsEntity::getDescription, trimmed));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(GoodsEntity::getStatus, status.trim());
        }
        if (StringUtils.hasText(auditStatus)) {
            wrapper.eq(GoodsEntity::getAuditStatus, auditStatus.trim());
        }

        List<GoodsEntity> goodsList = goodsMapper.selectList(wrapper);
        if (goodsList.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> categoryIds = new LinkedHashSet<>();
        Set<Long> sellerIds = new LinkedHashSet<>();
        for (GoodsEntity goods : goodsList) {
            categoryIds.add(goods.getCategoryId());
            sellerIds.add(goods.getSellerId());
        }

        Map<Long, CategoryEntity> categoryMap = new HashMap<>();
        for (CategoryEntity category : categoryMapper.selectBatchIds(categoryIds)) {
            categoryMap.put(category.getId(), category);
        }

        Map<Long, UserEntity> sellerMap = new HashMap<>();
        for (UserEntity seller : userMapper.selectBatchIds(sellerIds)) {
            sellerMap.put(seller.getId(), seller);
        }

        List<AdminGoodsManageItemResponse> result = new ArrayList<>();
        for (GoodsEntity goods : goodsList) {
            CategoryEntity category = categoryMap.get(goods.getCategoryId());
            UserEntity seller = sellerMap.get(goods.getSellerId());

            result.add(AdminGoodsManageItemResponse.builder()
                .goodsId(goods.getId())
                .title(goods.getTitle())
                .categoryName(category == null ? "未分类" : category.getName())
                .sellerName(resolveDisplayName(seller))
                .price(goods.getPrice())
                .status(goods.getStatus())
                .auditStatus(goods.getAuditStatus())
                .auditStatusLabel(resolveGoodsAuditStatusLabel(goods.getAuditStatus()))
                .auditNote(goods.getAuditNote())
                .auditTime(goods.getAuditTime())
                .contactInfo(goods.getContactInfo())
                .createdAt(goods.getCreatedAt())
                .build());
        }
        return result;
    }

    @Override
    public void offShelfGoods(Long goodsId) {
        GoodsEntity goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        goods.setStatus(GOODS_STATUS_OFF_SHELF);
        goodsMapper.updateById(goods);
    }

    @Override
    public void auditGoods(Long goodsId, String auditStatus, String auditNote) {
        String targetStatus = auditStatus == null ? "" : auditStatus.trim();
        if (!GOODS_AUDIT_APPROVED.equals(targetStatus) && !GOODS_AUDIT_REJECTED.equals(targetStatus)) {
            throw new BusinessException("商品审核状态仅支持通过或驳回");
        }
        GoodsEntity goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        goods.setAuditStatus(targetStatus);
        if (GOODS_AUDIT_APPROVED.equals(targetStatus)) {
            goods.setStatus(GOODS_STATUS_ON_SHELF);
            goods.setAuditNote(StringUtils.hasText(auditNote) ? sanitizeAuditNote(auditNote) : "管理员审核通过");
        } else {
            goods.setStatus(GOODS_STATUS_OFF_SHELF);
            goods.setAuditNote(StringUtils.hasText(auditNote) ? sanitizeAuditNote(auditNote) : "管理员审核驳回");
        }
        goods.setAuditTime(LocalDateTime.now());
        goodsMapper.updateById(goods);
    }

    @Override
    public List<AdminOrderManageItemResponse> listOrders(String status) {
        LambdaQueryWrapper<TradeOrderEntity> wrapper = new LambdaQueryWrapper<TradeOrderEntity>()
            .orderByDesc(TradeOrderEntity::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(TradeOrderEntity::getStatus, status.trim());
        }

        List<TradeOrderEntity> orders = tradeOrderMapper.selectList(wrapper);
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> goodsIds = new LinkedHashSet<>();
        Set<Long> userIds = new LinkedHashSet<>();
        for (TradeOrderEntity order : orders) {
            goodsIds.add(order.getGoodsId());
            userIds.add(order.getBuyerId());
            userIds.add(order.getSellerId());
        }

        Map<Long, GoodsEntity> goodsMap = new HashMap<>();
        for (GoodsEntity goods : goodsMapper.selectBatchIds(goodsIds)) {
            goodsMap.put(goods.getId(), goods);
        }

        Map<Long, UserEntity> userMap = new HashMap<>();
        for (UserEntity user : userMapper.selectBatchIds(userIds)) {
            userMap.put(user.getId(), user);
        }

        List<AdminOrderManageItemResponse> result = new ArrayList<>();
        for (TradeOrderEntity order : orders) {
            GoodsEntity goods = goodsMap.get(order.getGoodsId());
            UserEntity buyer = userMap.get(order.getBuyerId());
            UserEntity seller = userMap.get(order.getSellerId());

            result.add(AdminOrderManageItemResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .goodsId(order.getGoodsId())
                .goodsTitle(goods == null ? "商品已删除" : goods.getTitle())
                .buyerName(resolveDisplayName(buyer))
                .sellerName(resolveDisplayName(seller))
                .amount(order.getAmount())
                .status(order.getStatus())
                .statusLabel(resolveOrderStatusLabel(order.getStatus()))
                .meetupTime(order.getMeetupTime())
                .meetupLocation(order.getMeetupLocation())
                .meetupNote(order.getMeetupNote())
                .createdAt(order.getCreatedAt())
                .build());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long orderId, String status) {
        String targetStatus = status == null ? "" : status.trim();
        if (!ORDER_STATUS_PENDING.equals(targetStatus)
            && !ORDER_STATUS_SELLER_CONFIRMED.equals(targetStatus)
            && !ORDER_STATUS_COMPLETED.equals(targetStatus)
            && !ORDER_STATUS_CANCELLED.equals(targetStatus)) {
            throw new BusinessException("不支持的订单状态");
        }

        TradeOrderEntity order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(targetStatus);
        tradeOrderMapper.updateById(order);

        GoodsEntity goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            if (ORDER_STATUS_CANCELLED.equals(targetStatus)) {
                goods.setStatus(GOODS_STATUS_ON_SHELF);
            } else {
                goods.setStatus(GOODS_STATUS_OFF_SHELF);
            }
            goodsMapper.updateById(goods);
        }
    }

    @Override
    public List<AdminRentalOrderManageItemResponse> listRentalOrders(String status) {
        LambdaQueryWrapper<RentalOrderEntity> wrapper = new LambdaQueryWrapper<RentalOrderEntity>()
            .orderByDesc(RentalOrderEntity::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(RentalOrderEntity::getStatus, status.trim());
        }

        List<RentalOrderEntity> orders = rentalOrderMapper.selectList(wrapper);
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> itemIds = new LinkedHashSet<>();
        Set<Long> userIds = new LinkedHashSet<>();
        for (RentalOrderEntity order : orders) {
            itemIds.add(order.getRentalItemId());
            userIds.add(order.getRenterId());
            userIds.add(order.getOwnerId());
        }

        Map<Long, RentalItemEntity> itemMap = new HashMap<>();
        for (RentalItemEntity item : rentalItemMapper.selectBatchIds(itemIds)) {
            itemMap.put(item.getId(), item);
        }

        Map<Long, UserEntity> userMap = new HashMap<>();
        for (UserEntity user : userMapper.selectBatchIds(userIds)) {
            userMap.put(user.getId(), user);
        }

        List<AdminRentalOrderManageItemResponse> result = new ArrayList<>();
        for (RentalOrderEntity order : orders) {
            RentalItemEntity item = itemMap.get(order.getRentalItemId());
            UserEntity renter = userMap.get(order.getRenterId());
            UserEntity owner = userMap.get(order.getOwnerId());

            result.add(AdminRentalOrderManageItemResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .rentalItemId(order.getRentalItemId())
                .rentalTitle(item == null ? "租赁商品已删除" : item.getTitle())
                .renterName(resolveDisplayName(renter))
                .ownerName(resolveDisplayName(owner))
                .dailyRent(order.getDailyRent())
                .deposit(order.getDeposit())
                .totalAmount(order.getTotalAmount())
                .startDate(order.getStartDate())
                .endDate(order.getEndDate())
                .status(order.getStatus())
                .statusLabel(resolveRentalOrderStatusLabel(order.getStatus()))
                .createdAt(order.getCreatedAt())
                .build());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRentalOrderStatus(Long orderId, String status) {
        String targetStatus = status == null ? "" : status.trim();
        if (!RENTAL_ORDER_STATUS_PENDING.equals(targetStatus)
            && !RENTAL_ORDER_STATUS_ACTIVE.equals(targetStatus)
            && !RENTAL_ORDER_STATUS_COMPLETED.equals(targetStatus)
            && !RENTAL_ORDER_STATUS_CANCELLED.equals(targetStatus)) {
            throw new BusinessException("不支持的租赁订单状态");
        }

        RentalOrderEntity order = rentalOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("租赁订单不存在");
        }
        order.setStatus(targetStatus);
        rentalOrderMapper.updateById(order);

        RentalItemEntity item = rentalItemMapper.selectById(order.getRentalItemId());
        if (item != null) {
            if (RENTAL_ORDER_STATUS_COMPLETED.equals(targetStatus) || RENTAL_ORDER_STATUS_CANCELLED.equals(targetStatus)) {
                item.setStatus(RENTAL_ITEM_STATUS_AVAILABLE);
            } else {
                item.setStatus(RENTAL_ITEM_STATUS_RENTING);
            }
            rentalItemMapper.updateById(item);
        }
    }

    @Override
    public List<AdminDonationItemManageItemResponse> listDonationItems(String keyword, String status) {
        LambdaQueryWrapper<DonationItemEntity> wrapper = new LambdaQueryWrapper<DonationItemEntity>()
            .orderByDesc(DonationItemEntity::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            wrapper.and(w -> w.like(DonationItemEntity::getTitle, trimmed).or().like(DonationItemEntity::getDescription, trimmed));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(DonationItemEntity::getStatus, status.trim());
        }

        List<DonationItemEntity> items = donationItemMapper.selectList(wrapper);
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> donorIds = new LinkedHashSet<>();
        Set<Long> categoryIds = new LinkedHashSet<>();
        for (DonationItemEntity item : items) {
            donorIds.add(item.getDonorId());
            categoryIds.add(item.getCategoryId());
        }

        Map<Long, UserEntity> donorMap = new HashMap<>();
        for (UserEntity user : userMapper.selectBatchIds(donorIds)) {
            donorMap.put(user.getId(), user);
        }
        Map<Long, CategoryEntity> categoryMap = new HashMap<>();
        for (CategoryEntity category : categoryMapper.selectBatchIds(categoryIds)) {
            categoryMap.put(category.getId(), category);
        }

        List<AdminDonationItemManageItemResponse> result = new ArrayList<>();
        for (DonationItemEntity item : items) {
            result.add(AdminDonationItemManageItemResponse.builder()
                .itemId(item.getId())
                .title(item.getTitle())
                .donorName(resolveDisplayName(donorMap.get(item.getDonorId())))
                .categoryName(categoryMap.containsKey(item.getCategoryId()) ? categoryMap.get(item.getCategoryId()).getName() : "未分类")
                .contactInfo(item.getContactInfo())
                .pickupAddress(item.getPickupAddress())
                .status(item.getStatus())
                .statusLabel(resolveDonationItemStatusLabel(item.getStatus()))
                .createdAt(item.getCreatedAt())
                .build());
        }
        return result;
    }

    @Override
    public void updateDonationItemStatus(Long itemId, String status) {
        String targetStatus = status == null ? "" : status.trim();
        if (!DONATION_ITEM_STATUS_AVAILABLE.equals(targetStatus)
            && !DONATION_ITEM_STATUS_CLAIM_PENDING.equals(targetStatus)
            && !DONATION_ITEM_STATUS_CLAIMED.equals(targetStatus)
            && !DONATION_ITEM_STATUS_OFF_SHELF.equals(targetStatus)) {
            throw new BusinessException("不支持的捐赠物品状态");
        }
        DonationItemEntity item = donationItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("捐赠物品不存在");
        }
        item.setStatus(targetStatus);
        donationItemMapper.updateById(item);
    }

    @Override
    public List<AdminDonationRecordManageItemResponse> listDonationRecords(String status) {
        LambdaQueryWrapper<DonationRecordEntity> wrapper = new LambdaQueryWrapper<DonationRecordEntity>()
            .orderByDesc(DonationRecordEntity::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(DonationRecordEntity::getStatus, status.trim());
        }

        List<DonationRecordEntity> records = donationRecordMapper.selectList(wrapper);
        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> itemIds = new LinkedHashSet<>();
        Set<Long> userIds = new LinkedHashSet<>();
        for (DonationRecordEntity record : records) {
            itemIds.add(record.getDonationItemId());
            userIds.add(record.getDonorId());
            userIds.add(record.getClaimerId());
        }

        Map<Long, DonationItemEntity> itemMap = new HashMap<>();
        for (DonationItemEntity item : donationItemMapper.selectBatchIds(itemIds)) {
            itemMap.put(item.getId(), item);
        }

        Map<Long, UserEntity> userMap = new HashMap<>();
        for (UserEntity user : userMapper.selectBatchIds(userIds)) {
            userMap.put(user.getId(), user);
        }

        List<AdminDonationRecordManageItemResponse> result = new ArrayList<>();
        for (DonationRecordEntity record : records) {
            DonationItemEntity item = itemMap.get(record.getDonationItemId());
            result.add(AdminDonationRecordManageItemResponse.builder()
                .recordId(record.getId())
                .donationItemId(record.getDonationItemId())
                .donationTitle(item == null ? "捐赠物品已删除" : item.getTitle())
                .donorName(resolveDisplayName(userMap.get(record.getDonorId())))
                .claimerName(resolveDisplayName(userMap.get(record.getClaimerId())))
                .claimRemark(record.getClaimRemark())
                .status(record.getStatus())
                .statusLabel(resolveDonationRecordStatusLabel(record.getStatus()))
                .createdAt(record.getCreatedAt())
                .build());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDonationRecordStatus(Long recordId, String status) {
        String targetStatus = status == null ? "" : status.trim();
        if (!DONATION_RECORD_STATUS_PENDING.equals(targetStatus)
            && !DONATION_RECORD_STATUS_APPROVED.equals(targetStatus)
            && !DONATION_RECORD_STATUS_REJECTED.equals(targetStatus)
            && !DONATION_RECORD_STATUS_CANCELLED.equals(targetStatus)
            && !DONATION_RECORD_STATUS_COMPLETED.equals(targetStatus)) {
            throw new BusinessException("不支持的认领记录状态");
        }

        DonationRecordEntity record = donationRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("认领记录不存在");
        }
        record.setStatus(targetStatus);
        donationRecordMapper.updateById(record);

        DonationItemEntity item = donationItemMapper.selectById(record.getDonationItemId());
        if (item != null) {
            if (DONATION_RECORD_STATUS_APPROVED.equals(targetStatus) || DONATION_RECORD_STATUS_COMPLETED.equals(targetStatus)) {
                item.setStatus(DONATION_ITEM_STATUS_CLAIMED);
            } else if (DONATION_RECORD_STATUS_PENDING.equals(targetStatus)) {
                item.setStatus(DONATION_ITEM_STATUS_CLAIM_PENDING);
            } else {
                item.setStatus(DONATION_ITEM_STATUS_AVAILABLE);
            }
            donationItemMapper.updateById(item);
        }
    }

    private void ensureDefaultAdminUser() {
        Long count = adminUserMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        AdminUserEntity admin = new AdminUserEntity();
        admin.setUsername(DEFAULT_ADMIN_USERNAME);
        admin.setPassword(DEFAULT_ADMIN_PASSWORD_HASH);
        admin.setNickname("系统管理员");
        admin.setStatus(1);
        adminUserMapper.insert(admin);
    }

    private Long orZero(Long value) {
        return value == null ? 0L : value;
    }

    private String limit(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String resolveDisplayName(UserEntity user) {
        if (user == null) {
            return "未知用户";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername().trim();
        }
        return "未知用户";
    }

    private String resolveOrderStatusLabel(String status) {
        if (ORDER_STATUS_PENDING.equals(status)) {
            return "待卖家确认";
        }
        if (ORDER_STATUS_SELLER_CONFIRMED.equals(status)) {
            return "待买家确认收货";
        }
        if (ORDER_STATUS_COMPLETED.equals(status)) {
            return "已完成";
        }
        if (ORDER_STATUS_CANCELLED.equals(status)) {
            return "已取消";
        }
        return "未知状态";
    }

    private String resolveUserAuditStatusLabel(String status) {
        if (USER_AUDIT_PENDING.equals(status)) {
            return "待审核";
        }
        if (USER_AUDIT_APPROVED.equals(status)) {
            return "已通过";
        }
        if (USER_AUDIT_REJECTED.equals(status)) {
            return "已驳回";
        }
        return "未知";
    }

    private String sanitizeAuditNote(String auditNote) {
        if (!StringUtils.hasText(auditNote)) {
            return null;
        }
        String normalized = auditNote.trim();
        if (normalized.length() <= 255) {
            return normalized;
        }
        return normalized.substring(0, 255);
    }

    private String resolveRentalOrderStatusLabel(String status) {
        if (RENTAL_ORDER_STATUS_PENDING.equals(status)) {
            return "待出租方确认";
        }
        if (RENTAL_ORDER_STATUS_ACTIVE.equals(status)) {
            return "租赁进行中";
        }
        if (RENTAL_ORDER_STATUS_COMPLETED.equals(status)) {
            return "已完成";
        }
        if (RENTAL_ORDER_STATUS_CANCELLED.equals(status)) {
            return "已取消";
        }
        return "未知状态";
    }

    private String resolveDonationItemStatusLabel(String status) {
        if (DONATION_ITEM_STATUS_AVAILABLE.equals(status)) {
            return "可认领";
        }
        if (DONATION_ITEM_STATUS_CLAIM_PENDING.equals(status)) {
            return "认领申请中";
        }
        if (DONATION_ITEM_STATUS_CLAIMED.equals(status)) {
            return "已被认领";
        }
        if (DONATION_ITEM_STATUS_OFF_SHELF.equals(status)) {
            return "已下架";
        }
        return "未知状态";
    }

    private String resolveDonationRecordStatusLabel(String status) {
        if (DONATION_RECORD_STATUS_PENDING.equals(status)) {
            return "待处理";
        }
        if (DONATION_RECORD_STATUS_APPROVED.equals(status)) {
            return "已同意";
        }
        if (DONATION_RECORD_STATUS_REJECTED.equals(status)) {
            return "已拒绝";
        }
        if (DONATION_RECORD_STATUS_CANCELLED.equals(status)) {
            return "已取消";
        }
        if (DONATION_RECORD_STATUS_COMPLETED.equals(status)) {
            return "已完成";
        }
        return "未知状态";
    }

    private String resolveGoodsAuditStatusLabel(String status) {
        if (GOODS_AUDIT_PENDING.equals(status)) {
            return "待审核";
        }
        if (GOODS_AUDIT_APPROVED.equals(status)) {
            return "已通过";
        }
        if (GOODS_AUDIT_REJECTED.equals(status)) {
            return "已驳回";
        }
        return "未知";
    }
}

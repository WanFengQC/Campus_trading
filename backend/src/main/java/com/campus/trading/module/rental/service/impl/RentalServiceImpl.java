package com.campus.trading.module.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.category.entity.CategoryEntity;
import com.campus.trading.module.category.mapper.CategoryMapper;
import com.campus.trading.module.rental.dto.RentalItemResponse;
import com.campus.trading.module.rental.dto.RentalItemSaveRequest;
import com.campus.trading.module.rental.dto.RentalOrderResponse;
import com.campus.trading.module.rental.entity.RentalItemEntity;
import com.campus.trading.module.rental.entity.RentalOrderEntity;
import com.campus.trading.module.rental.mapper.RentalItemMapper;
import com.campus.trading.module.rental.mapper.RentalOrderMapper;
import com.campus.trading.module.rental.service.RentalService;
import com.campus.trading.module.user.entity.UserEntity;
import com.campus.trading.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RentalServiceImpl implements RentalService {

    private static final String ITEM_STATUS_AVAILABLE = "AVAILABLE";
    private static final String ITEM_STATUS_RENTING = "RENTING";
    private static final String ITEM_STATUS_OFF_SHELF = "OFF_SHELF";
    private static final String ORDER_STATUS_PENDING = "PENDING";
    private static final String ORDER_STATUS_ACTIVE = "ACTIVE";
    private static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    private final RentalItemMapper rentalItemMapper;
    private final RentalOrderMapper rentalOrderMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public RentalServiceImpl(RentalItemMapper rentalItemMapper,
                             RentalOrderMapper rentalOrderMapper,
                             CategoryMapper categoryMapper,
                             UserMapper userMapper) {
        this.rentalItemMapper = rentalItemMapper;
        this.rentalOrderMapper = rentalOrderMapper;
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<RentalItemResponse> searchAvailable(String keyword, Long categoryId, BigDecimal minDailyRent, BigDecimal maxDailyRent) {
        LambdaQueryWrapper<RentalItemEntity> wrapper = new LambdaQueryWrapper<RentalItemEntity>()
            .eq(RentalItemEntity::getStatus, ITEM_STATUS_AVAILABLE)
            .orderByDesc(RentalItemEntity::getCreatedAt);

        if (categoryId != null) {
            wrapper.eq(RentalItemEntity::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            wrapper.and(w -> w.like(RentalItemEntity::getTitle, trimmed).or().like(RentalItemEntity::getDescription, trimmed));
        }
        if (minDailyRent != null) {
            wrapper.ge(RentalItemEntity::getDailyRent, minDailyRent);
        }
        if (maxDailyRent != null) {
            wrapper.le(RentalItemEntity::getDailyRent, maxDailyRent);
        }

        return toItemResponses(rentalItemMapper.selectList(wrapper));
    }

    @Override
    public RentalItemResponse getDetail(Long itemId) {
        RentalItemEntity item = rentalItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("租赁商品不存在");
        }
        return toItemResponse(item);
    }

    @Override
    public RentalItemResponse createItem(Long ownerId, RentalItemSaveRequest request) {
        RentalItemEntity item = new RentalItemEntity();
        applyItemRequest(item, request);
        item.setOwnerId(ownerId);
        item.setStatus(ITEM_STATUS_AVAILABLE);
        rentalItemMapper.insert(item);
        return getDetail(item.getId());
    }

    @Override
    public RentalItemResponse updateItem(Long ownerId, Long itemId, RentalItemSaveRequest request) {
        RentalItemEntity item = getOwnerItemOrThrow(ownerId, itemId);
        if (!ITEM_STATUS_AVAILABLE.equals(item.getStatus()) && !ITEM_STATUS_OFF_SHELF.equals(item.getStatus())) {
            throw new BusinessException("当前状态不可编辑");
        }
        applyItemRequest(item, request);
        rentalItemMapper.updateById(item);
        return getDetail(item.getId());
    }

    @Override
    public void offShelf(Long ownerId, Long itemId) {
        RentalItemEntity item = getOwnerItemOrThrow(ownerId, itemId);
        if (ITEM_STATUS_RENTING.equals(item.getStatus())) {
            throw new BusinessException("租赁中商品不可下架");
        }
        item.setStatus(ITEM_STATUS_OFF_SHELF);
        rentalItemMapper.updateById(item);
    }

    @Override
    public List<RentalItemResponse> listOwnerItems(Long ownerId) {
        List<RentalItemEntity> items = rentalItemMapper.selectList(new LambdaQueryWrapper<RentalItemEntity>()
            .eq(RentalItemEntity::getOwnerId, ownerId)
            .orderByDesc(RentalItemEntity::getCreatedAt));
        return toItemResponses(items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long renterId, Long itemId, LocalDate startDate, LocalDate endDate, String renterRemark) {
        validateDates(startDate, endDate);

        RentalItemEntity item = rentalItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("租赁商品不存在");
        }
        if (renterId.equals(item.getOwnerId())) {
            throw new BusinessException("不能租赁自己发布的商品");
        }
        if (!ITEM_STATUS_AVAILABLE.equals(item.getStatus())) {
            throw new BusinessException("该租赁商品当前不可下单");
        }

        int updated = rentalItemMapper.update(null, new LambdaUpdateWrapper<RentalItemEntity>()
            .set(RentalItemEntity::getStatus, ITEM_STATUS_RENTING)
            .eq(RentalItemEntity::getId, item.getId())
            .eq(RentalItemEntity::getStatus, ITEM_STATUS_AVAILABLE));
        if (updated != 1) {
            throw new BusinessException("该租赁商品已被其他用户抢先下单");
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal total = item.getDailyRent().multiply(BigDecimal.valueOf(days)).add(item.getDeposit());

        RentalOrderEntity order = new RentalOrderEntity();
        order.setOrderNo(generateOrderNo());
        order.setRentalItemId(item.getId());
        order.setRenterId(renterId);
        order.setOwnerId(item.getOwnerId());
        order.setDailyRent(item.getDailyRent());
        order.setDeposit(item.getDeposit());
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setTotalAmount(total);
        order.setRenterRemark(normalizeRemark(renterRemark));
        order.setStatus(ORDER_STATUS_PENDING);
        rentalOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    public List<RentalOrderResponse> listUserOrders(Long userId) {
        List<RentalOrderEntity> orders = rentalOrderMapper.selectList(new LambdaQueryWrapper<RentalOrderEntity>()
            .and(w -> w.eq(RentalOrderEntity::getRenterId, userId).or().eq(RentalOrderEntity::getOwnerId, userId))
            .orderByDesc(RentalOrderEntity::getCreatedAt));
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

        List<RentalOrderResponse> results = new ArrayList<>();
        for (RentalOrderEntity order : orders) {
            RentalItemEntity item = itemMap.get(order.getRentalItemId());
            UserEntity renter = userMap.get(order.getRenterId());
            UserEntity owner = userMap.get(order.getOwnerId());

            results.add(RentalOrderResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .rentalItemId(order.getRentalItemId())
                .rentalTitle(item == null ? "租赁商品已删除" : item.getTitle())
                .rentalCoverImageUrl(item == null ? null : item.getCoverImageUrl())
                .renterId(order.getRenterId())
                .renterName(resolveDisplayName(renter))
                .ownerId(order.getOwnerId())
                .ownerName(resolveDisplayName(owner))
                .dailyRent(order.getDailyRent())
                .deposit(order.getDeposit())
                .startDate(order.getStartDate())
                .endDate(order.getEndDate())
                .totalAmount(order.getTotalAmount())
                .renterRemark(order.getRenterRemark())
                .status(order.getStatus())
                .statusLabel(resolveOrderStatusLabel(order.getStatus()))
                .renterSide(userId.equals(order.getRenterId()))
                .ownerSide(userId.equals(order.getOwnerId()))
                .createdAt(order.getCreatedAt())
                .build());
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long renterId, Long orderId) {
        RentalOrderEntity order = getOrderOrThrow(orderId);
        if (!renterId.equals(order.getRenterId())) {
            throw new BusinessException("无权取消该订单");
        }
        if (!ORDER_STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可取消");
        }

        order.setStatus(ORDER_STATUS_CANCELLED);
        rentalOrderMapper.updateById(order);
        setItemAvailable(order.getRentalItemId());
    }

    @Override
    public void ownerConfirm(Long ownerId, Long orderId) {
        RentalOrderEntity order = getOrderOrThrow(orderId);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new BusinessException("无权操作该订单");
        }
        if (!ORDER_STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可确认");
        }
        order.setStatus(ORDER_STATUS_ACTIVE);
        rentalOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renterComplete(Long renterId, Long orderId) {
        RentalOrderEntity order = getOrderOrThrow(orderId);
        if (!renterId.equals(order.getRenterId())) {
            throw new BusinessException("无权操作该订单");
        }
        if (!ORDER_STATUS_ACTIVE.equals(order.getStatus())) {
            throw new BusinessException("仅可完成进行中的订单");
        }
        order.setStatus(ORDER_STATUS_COMPLETED);
        rentalOrderMapper.updateById(order);
        setItemAvailable(order.getRentalItemId());
    }

    @Override
    public void renewOrder(Long renterId, Long orderId, LocalDate newEndDate) {
        RentalOrderEntity order = getOrderOrThrow(orderId);
        if (!renterId.equals(order.getRenterId())) {
            throw new BusinessException("无权续租该订单");
        }
        if (!ORDER_STATUS_ACTIVE.equals(order.getStatus())) {
            throw new BusinessException("仅进行中的订单可续租");
        }
        if (newEndDate == null || !newEndDate.isAfter(order.getEndDate())) {
            throw new BusinessException("续租结束日期必须晚于当前结束日期");
        }

        long extraDays = ChronoUnit.DAYS.between(order.getEndDate(), newEndDate);
        BigDecimal extraAmount = order.getDailyRent().multiply(BigDecimal.valueOf(extraDays));
        order.setEndDate(newEndDate);
        order.setTotalAmount(order.getTotalAmount().add(extraAmount));
        rentalOrderMapper.updateById(order);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("请选择租赁起止日期");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
    }

    private void setItemAvailable(Long itemId) {
        rentalItemMapper.update(null, new LambdaUpdateWrapper<RentalItemEntity>()
            .set(RentalItemEntity::getStatus, ITEM_STATUS_AVAILABLE)
            .eq(RentalItemEntity::getId, itemId)
            .eq(RentalItemEntity::getStatus, ITEM_STATUS_RENTING));
    }

    private RentalOrderEntity getOrderOrThrow(Long orderId) {
        RentalOrderEntity order = rentalOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("租赁订单不存在");
        }
        return order;
    }

    private String generateOrderNo() {
        long millis = System.currentTimeMillis();
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "RO" + millis + suffix;
    }

    private String normalizeRemark(String remark) {
        if (!StringUtils.hasText(remark)) {
            return null;
        }
        String trimmed = remark.trim();
        if (trimmed.length() <= 255) {
            return trimmed;
        }
        return trimmed.substring(0, 255);
    }

    private RentalItemEntity getOwnerItemOrThrow(Long ownerId, Long itemId) {
        RentalItemEntity item = rentalItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("租赁商品不存在");
        }
        if (!ownerId.equals(item.getOwnerId())) {
            throw new BusinessException("无权操作该租赁商品");
        }
        return item;
    }

    private void applyItemRequest(RentalItemEntity item, RentalItemSaveRequest request) {
        CategoryEntity category = categoryMapper.selectById(request.getCategoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException("分类不存在或已停用");
        }
        item.setCategoryId(request.getCategoryId());
        item.setTitle(request.getTitle().trim());
        item.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        item.setDailyRent(request.getDailyRent());
        item.setDeposit(request.getDeposit() == null ? BigDecimal.ZERO : request.getDeposit());
        item.setContactInfo(request.getContactInfo().trim());
        item.setCoverImageUrl(request.getCoverImageUrl() == null ? null : request.getCoverImageUrl().trim());
    }

    private List<RentalItemResponse> toItemResponses(List<RentalItemEntity> items) {
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> ownerIds = new LinkedHashSet<>();
        Map<Long, String> categoryNames = new HashMap<>();
        for (CategoryEntity category : categoryMapper.selectList(null)) {
            categoryNames.put(category.getId(), category.getName());
        }
        for (RentalItemEntity item : items) {
            ownerIds.add(item.getOwnerId());
        }

        Map<Long, UserEntity> ownerMap = new HashMap<>();
        for (UserEntity owner : userMapper.selectBatchIds(ownerIds)) {
            ownerMap.put(owner.getId(), owner);
        }

        List<RentalItemResponse> results = new ArrayList<>();
        for (RentalItemEntity item : items) {
            UserEntity owner = ownerMap.get(item.getOwnerId());
            results.add(RentalItemResponse.builder()
                .id(item.getId())
                .ownerId(item.getOwnerId())
                .ownerName(resolveDisplayName(owner))
                .categoryId(item.getCategoryId())
                .categoryName(categoryNames.getOrDefault(item.getCategoryId(), "未分类"))
                .title(item.getTitle())
                .description(item.getDescription())
                .dailyRent(item.getDailyRent())
                .deposit(item.getDeposit())
                .contactInfo(item.getContactInfo())
                .coverImageUrl(item.getCoverImageUrl())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .build());
        }
        return results;
    }

    private RentalItemResponse toItemResponse(RentalItemEntity item) {
        CategoryEntity category = categoryMapper.selectById(item.getCategoryId());
        UserEntity owner = userMapper.selectById(item.getOwnerId());
        return RentalItemResponse.builder()
            .id(item.getId())
            .ownerId(item.getOwnerId())
            .ownerName(resolveDisplayName(owner))
            .categoryId(item.getCategoryId())
            .categoryName(category == null ? "未分类" : category.getName())
            .title(item.getTitle())
            .description(item.getDescription())
            .dailyRent(item.getDailyRent())
            .deposit(item.getDeposit())
            .contactInfo(item.getContactInfo())
            .coverImageUrl(item.getCoverImageUrl())
            .status(item.getStatus())
            .createdAt(item.getCreatedAt())
            .build();
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
            return "待出租方确认";
        }
        if (ORDER_STATUS_ACTIVE.equals(status)) {
            return "租赁进行中";
        }
        if (ORDER_STATUS_COMPLETED.equals(status)) {
            return "已完成";
        }
        if (ORDER_STATUS_CANCELLED.equals(status)) {
            return "已取消";
        }
        return "未知状态";
    }
}

package com.campus.trading.module.donation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.category.entity.CategoryEntity;
import com.campus.trading.module.category.mapper.CategoryMapper;
import com.campus.trading.module.donation.dto.DonationItemResponse;
import com.campus.trading.module.donation.dto.DonationItemSaveRequest;
import com.campus.trading.module.donation.dto.DonationRecordResponse;
import com.campus.trading.module.donation.entity.DonationItemEntity;
import com.campus.trading.module.donation.entity.DonationRecordEntity;
import com.campus.trading.module.donation.mapper.DonationItemMapper;
import com.campus.trading.module.donation.mapper.DonationRecordMapper;
import com.campus.trading.module.donation.service.DonationService;
import com.campus.trading.module.user.entity.UserEntity;
import com.campus.trading.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DonationServiceImpl implements DonationService {

    private static final String ITEM_STATUS_AVAILABLE = "AVAILABLE";
    private static final String ITEM_STATUS_CLAIM_PENDING = "CLAIM_PENDING";
    private static final String ITEM_STATUS_CLAIMED = "CLAIMED";
    private static final String ITEM_STATUS_OFF_SHELF = "OFF_SHELF";

    private static final String RECORD_STATUS_PENDING = "PENDING";
    private static final String RECORD_STATUS_APPROVED = "APPROVED";
    private static final String RECORD_STATUS_REJECTED = "REJECTED";
    private static final String RECORD_STATUS_CANCELLED = "CANCELLED";
    private static final String RECORD_STATUS_COMPLETED = "COMPLETED";

    private final DonationItemMapper donationItemMapper;
    private final DonationRecordMapper donationRecordMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public DonationServiceImpl(DonationItemMapper donationItemMapper,
                               DonationRecordMapper donationRecordMapper,
                               CategoryMapper categoryMapper,
                               UserMapper userMapper) {
        this.donationItemMapper = donationItemMapper;
        this.donationRecordMapper = donationRecordMapper;
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<DonationItemResponse> searchAvailable(String keyword, Long categoryId) {
        LambdaQueryWrapper<DonationItemEntity> wrapper = new LambdaQueryWrapper<DonationItemEntity>()
            .eq(DonationItemEntity::getStatus, ITEM_STATUS_AVAILABLE)
            .orderByDesc(DonationItemEntity::getCreatedAt);

        if (categoryId != null) {
            wrapper.eq(DonationItemEntity::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            wrapper.and(w -> w.like(DonationItemEntity::getTitle, trimmed)
                .or()
                .like(DonationItemEntity::getDescription, trimmed));
        }
        return toItemResponses(donationItemMapper.selectList(wrapper));
    }

    @Override
    public DonationItemResponse getDetail(Long itemId) {
        DonationItemEntity item = donationItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("捐赠物品不存在");
        }
        return toItemResponse(item);
    }

    @Override
    public DonationItemResponse createItem(Long donorId, DonationItemSaveRequest request) {
        DonationItemEntity item = new DonationItemEntity();
        applyItemRequest(item, request);
        item.setDonorId(donorId);
        item.setStatus(ITEM_STATUS_AVAILABLE);
        donationItemMapper.insert(item);
        return getDetail(item.getId());
    }

    @Override
    public void offShelf(Long donorId, Long itemId) {
        DonationItemEntity item = getDonorItemOrThrow(donorId, itemId);
        if (!ITEM_STATUS_AVAILABLE.equals(item.getStatus())) {
            throw new BusinessException("仅可下架可认领状态的物品");
        }
        item.setStatus(ITEM_STATUS_OFF_SHELF);
        donationItemMapper.updateById(item);
    }

    @Override
    public List<DonationItemResponse> listDonorItems(Long donorId) {
        List<DonationItemEntity> items = donationItemMapper.selectList(new LambdaQueryWrapper<DonationItemEntity>()
            .eq(DonationItemEntity::getDonorId, donorId)
            .orderByDesc(DonationItemEntity::getCreatedAt));
        return toItemResponses(items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long claim(Long claimerId, Long itemId, String claimRemark) {
        DonationItemEntity item = donationItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("捐赠物品不存在");
        }
        if (claimerId.equals(item.getDonorId())) {
            throw new BusinessException("不能认领自己发布的捐赠物品");
        }
        if (!ITEM_STATUS_AVAILABLE.equals(item.getStatus())) {
            throw new BusinessException("该捐赠物品当前不可认领");
        }

        int updated = setItemStatus(item.getId(), ITEM_STATUS_AVAILABLE, ITEM_STATUS_CLAIM_PENDING);
        if (updated != 1) {
            throw new BusinessException("该捐赠物品已被其他用户先申请");
        }

        DonationRecordEntity record = new DonationRecordEntity();
        record.setDonationItemId(item.getId());
        record.setClaimerId(claimerId);
        record.setDonorId(item.getDonorId());
        record.setClaimRemark(normalizeOptionalText(claimRemark, 255));
        record.setStatus(RECORD_STATUS_PENDING);
        donationRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public List<DonationRecordResponse> listUserRecords(Long userId) {
        List<DonationRecordEntity> records = donationRecordMapper.selectList(new LambdaQueryWrapper<DonationRecordEntity>()
            .and(w -> w.eq(DonationRecordEntity::getDonorId, userId).or().eq(DonationRecordEntity::getClaimerId, userId))
            .orderByDesc(DonationRecordEntity::getCreatedAt));
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

        List<DonationRecordResponse> result = new ArrayList<>();
        for (DonationRecordEntity record : records) {
            DonationItemEntity item = itemMap.get(record.getDonationItemId());
            UserEntity donor = userMap.get(record.getDonorId());
            UserEntity claimer = userMap.get(record.getClaimerId());

            result.add(DonationRecordResponse.builder()
                .recordId(record.getId())
                .donationItemId(record.getDonationItemId())
                .donationTitle(item == null ? "捐赠物品已删除" : item.getTitle())
                .donationCoverImageUrl(item == null ? null : item.getCoverImageUrl())
                .donorId(record.getDonorId())
                .donorName(resolveDisplayName(donor))
                .claimerId(record.getClaimerId())
                .claimerName(resolveDisplayName(claimer))
                .claimRemark(record.getClaimRemark())
                .status(record.getStatus())
                .statusLabel(resolveRecordStatusLabel(record.getStatus()))
                .donorSide(userId.equals(record.getDonorId()))
                .claimerSide(userId.equals(record.getClaimerId()))
                .createdAt(record.getCreatedAt())
                .build());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelClaim(Long claimerId, Long recordId) {
        DonationRecordEntity record = getRecordOrThrow(recordId);
        if (!claimerId.equals(record.getClaimerId())) {
            throw new BusinessException("无权取消该认领申请");
        }
        if (!RECORD_STATUS_PENDING.equals(record.getStatus())) {
            throw new BusinessException("当前申请状态不可取消");
        }

        record.setStatus(RECORD_STATUS_CANCELLED);
        donationRecordMapper.updateById(record);
        setItemStatus(record.getDonationItemId(), ITEM_STATUS_CLAIM_PENDING, ITEM_STATUS_AVAILABLE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveClaim(Long donorId, Long recordId) {
        DonationRecordEntity record = getRecordOrThrow(recordId);
        if (!donorId.equals(record.getDonorId())) {
            throw new BusinessException("无权处理该认领申请");
        }
        if (!RECORD_STATUS_PENDING.equals(record.getStatus())) {
            throw new BusinessException("当前申请状态不可同意");
        }

        record.setStatus(RECORD_STATUS_APPROVED);
        donationRecordMapper.updateById(record);
        int updated = setItemStatus(record.getDonationItemId(), ITEM_STATUS_CLAIM_PENDING, ITEM_STATUS_CLAIMED);
        if (updated != 1) {
            throw new BusinessException("当前物品状态已变化，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectClaim(Long donorId, Long recordId) {
        DonationRecordEntity record = getRecordOrThrow(recordId);
        if (!donorId.equals(record.getDonorId())) {
            throw new BusinessException("无权处理该认领申请");
        }
        if (!RECORD_STATUS_PENDING.equals(record.getStatus())) {
            throw new BusinessException("当前申请状态不可拒绝");
        }

        record.setStatus(RECORD_STATUS_REJECTED);
        donationRecordMapper.updateById(record);
        setItemStatus(record.getDonationItemId(), ITEM_STATUS_CLAIM_PENDING, ITEM_STATUS_AVAILABLE);
    }

    @Override
    public void completeClaim(Long claimerId, Long recordId) {
        DonationRecordEntity record = getRecordOrThrow(recordId);
        if (!claimerId.equals(record.getClaimerId())) {
            throw new BusinessException("无权完成该认领记录");
        }
        if (!RECORD_STATUS_APPROVED.equals(record.getStatus())) {
            throw new BusinessException("仅可完成已同意的认领记录");
        }
        record.setStatus(RECORD_STATUS_COMPLETED);
        donationRecordMapper.updateById(record);
    }

    private DonationRecordEntity getRecordOrThrow(Long recordId) {
        DonationRecordEntity record = donationRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("认领记录不存在");
        }
        return record;
    }

    private DonationItemEntity getDonorItemOrThrow(Long donorId, Long itemId) {
        DonationItemEntity item = donationItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("捐赠物品不存在");
        }
        if (!donorId.equals(item.getDonorId())) {
            throw new BusinessException("无权操作该捐赠物品");
        }
        return item;
    }

    private DonationItemResponse toItemResponse(DonationItemEntity item) {
        List<DonationItemResponse> responses = toItemResponses(List.of(item));
        if (responses.isEmpty()) {
            throw new BusinessException("捐赠物品不存在");
        }
        return responses.get(0);
    }

    private List<DonationItemResponse> toItemResponses(List<DonationItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> categoryIds = new LinkedHashSet<>();
        Set<Long> donorIds = new LinkedHashSet<>();
        for (DonationItemEntity item : items) {
            categoryIds.add(item.getCategoryId());
            donorIds.add(item.getDonorId());
        }

        Map<Long, CategoryEntity> categoryMap = new HashMap<>();
        for (CategoryEntity category : categoryMapper.selectBatchIds(categoryIds)) {
            categoryMap.put(category.getId(), category);
        }

        Map<Long, UserEntity> donorMap = new HashMap<>();
        for (UserEntity donor : userMapper.selectBatchIds(donorIds)) {
            donorMap.put(donor.getId(), donor);
        }

        List<DonationItemResponse> result = new ArrayList<>();
        for (DonationItemEntity item : items) {
            CategoryEntity category = categoryMap.get(item.getCategoryId());
            UserEntity donor = donorMap.get(item.getDonorId());

            result.add(DonationItemResponse.builder()
                .id(item.getId())
                .donorId(item.getDonorId())
                .donorName(resolveDisplayName(donor))
                .categoryId(item.getCategoryId())
                .categoryName(category == null ? "未分类" : category.getName())
                .title(item.getTitle())
                .description(item.getDescription())
                .contactInfo(item.getContactInfo())
                .pickupAddress(item.getPickupAddress())
                .coverImageUrl(item.getCoverImageUrl())
                .status(item.getStatus())
                .statusLabel(resolveItemStatusLabel(item.getStatus()))
                .createdAt(item.getCreatedAt())
                .build());
        }
        return result;
    }

    private int setItemStatus(Long itemId, String fromStatus, String toStatus) {
        return donationItemMapper.update(null, new LambdaUpdateWrapper<DonationItemEntity>()
            .set(DonationItemEntity::getStatus, toStatus)
            .eq(DonationItemEntity::getId, itemId)
            .eq(DonationItemEntity::getStatus, fromStatus));
    }

    private void applyItemRequest(DonationItemEntity item, DonationItemSaveRequest request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }

        if (request.getCategoryId() == null) {
            throw new BusinessException("请选择捐赠分类");
        }
        CategoryEntity category = categoryMapper.selectById(request.getCategoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException("分类不存在或已停用");
        }

        item.setCategoryId(request.getCategoryId());
        item.setTitle(normalizeRequiredText(request.getTitle(), 128, "标题不能为空"));
        item.setDescription(normalizeOptionalText(request.getDescription(), 2000));
        item.setContactInfo(normalizeRequiredText(request.getContactInfo(), 64, "联系方式不能为空"));
        item.setPickupAddress(normalizeOptionalText(request.getPickupAddress(), 255));
        item.setCoverImageUrl(normalizeOptionalText(request.getCoverImageUrl(), 255));
    }

    private String normalizeRequiredText(String value, int maxLength, String emptyMessage) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(emptyMessage);
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
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

    private String resolveItemStatusLabel(String status) {
        if (ITEM_STATUS_AVAILABLE.equals(status)) {
            return "可认领";
        }
        if (ITEM_STATUS_CLAIM_PENDING.equals(status)) {
            return "认领申请中";
        }
        if (ITEM_STATUS_CLAIMED.equals(status)) {
            return "已被认领";
        }
        if (ITEM_STATUS_OFF_SHELF.equals(status)) {
            return "已下架";
        }
        return status;
    }

    private String resolveRecordStatusLabel(String status) {
        if (RECORD_STATUS_PENDING.equals(status)) {
            return "待处理";
        }
        if (RECORD_STATUS_APPROVED.equals(status)) {
            return "已同意";
        }
        if (RECORD_STATUS_REJECTED.equals(status)) {
            return "已拒绝";
        }
        if (RECORD_STATUS_CANCELLED.equals(status)) {
            return "已取消";
        }
        if (RECORD_STATUS_COMPLETED.equals(status)) {
            return "已完成";
        }
        return status;
    }
}

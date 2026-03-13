package com.campus.trading.module.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.category.entity.CategoryEntity;
import com.campus.trading.module.category.mapper.CategoryMapper;
import com.campus.trading.module.goods.dto.GoodsDetailResponse;
import com.campus.trading.module.goods.dto.GoodsListItemResponse;
import com.campus.trading.module.goods.dto.GoodsQueryRequest;
import com.campus.trading.module.goods.dto.GoodsSaveRequest;
import com.campus.trading.module.goods.entity.GoodsEntity;
import com.campus.trading.module.goods.entity.GoodsImageEntity;
import com.campus.trading.module.goods.mapper.GoodsImageMapper;
import com.campus.trading.module.goods.mapper.GoodsMapper;
import com.campus.trading.module.goods.service.GoodsService;
import com.campus.trading.module.user.entity.UserEntity;
import com.campus.trading.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoodsServiceImpl implements GoodsService {

    private static final String STATUS_ON_SHELF = "ON_SHELF";
    private static final String STATUS_OFF_SHELF = "OFF_SHELF";
    private static final String AUDIT_PENDING = "PENDING";
    private static final String AUDIT_APPROVED = "APPROVED";
    private static final String AUDIT_REJECTED = "REJECTED";
    private static final int MAX_IMAGE_COUNT = 9;

    private final GoodsMapper goodsMapper;
    private final GoodsImageMapper goodsImageMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public GoodsServiceImpl(GoodsMapper goodsMapper,
                            GoodsImageMapper goodsImageMapper,
                            CategoryMapper categoryMapper,
                            UserMapper userMapper) {
        this.goodsMapper = goodsMapper;
        this.goodsImageMapper = goodsImageMapper;
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<GoodsListItemResponse> searchOnShelfGoods(GoodsQueryRequest request) {
        LambdaQueryWrapper<GoodsEntity> wrapper = new LambdaQueryWrapper<GoodsEntity>()
            .eq(GoodsEntity::getStatus, STATUS_ON_SHELF)
            .eq(GoodsEntity::getAuditStatus, AUDIT_APPROVED)
            .orderByDesc(GoodsEntity::getCreatedAt);

        if (request.getCategoryId() != null) {
            wrapper.eq(GoodsEntity::getCategoryId, request.getCategoryId());
        }
        if (StringUtils.hasText(request.getConditionLevel())) {
            wrapper.eq(GoodsEntity::getConditionLevel, request.getConditionLevel().trim());
        }
        if (request.getMinPrice() != null) {
            wrapper.ge(GoodsEntity::getPrice, request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            wrapper.le(GoodsEntity::getPrice, request.getMaxPrice());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            wrapper.and(w -> w.like(GoodsEntity::getTitle, keyword).or().like(GoodsEntity::getDescription, keyword));
        }

        List<GoodsEntity> goods = goodsMapper.selectList(wrapper);
        return toListItems(goods);
    }

    @Override
    public GoodsDetailResponse getGoodsDetail(Long goodsId) {
        return getGoodsDetailForViewer(goodsId, null);
    }

    @Override
    public GoodsDetailResponse getGoodsDetailForViewer(Long goodsId, Long viewerUserId) {
        GoodsEntity goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        boolean isSeller = viewerUserId != null && viewerUserId.equals(goods.getSellerId());
        boolean publiclyVisible = STATUS_ON_SHELF.equals(goods.getStatus()) && AUDIT_APPROVED.equals(goods.getAuditStatus());
        if (!isSeller && !publiclyVisible) {
            throw new BusinessException("商品未通过审核或已下架");
        }

        CategoryEntity category = categoryMapper.selectById(goods.getCategoryId());
        UserEntity seller = userMapper.selectById(goods.getSellerId());
        List<String> imageUrls = listGoodsImageUrls(goods.getId());
        String coverImageUrl = goods.getCoverImageUrl();
        if (!StringUtils.hasText(coverImageUrl) && !imageUrls.isEmpty()) {
            coverImageUrl = imageUrls.get(0);
        }
        if (imageUrls.isEmpty() && StringUtils.hasText(coverImageUrl)) {
            imageUrls.add(coverImageUrl);
        }

        return GoodsDetailResponse.builder()
            .id(goods.getId())
            .sellerId(goods.getSellerId())
            .sellerName(seller == null ? "未知用户"
                : (StringUtils.hasText(seller.getNickname()) ? seller.getNickname() : seller.getUsername()))
            .categoryId(goods.getCategoryId())
            .categoryName(category == null ? "未分类" : category.getName())
            .title(goods.getTitle())
            .description(goods.getDescription())
            .price(goods.getPrice())
            .conditionLevel(goods.getConditionLevel())
            .contactInfo(goods.getContactInfo())
            .coverImageUrl(coverImageUrl)
            .imageUrls(imageUrls)
            .status(goods.getStatus())
            .auditStatus(goods.getAuditStatus())
            .auditStatusLabel(resolveAuditStatusLabel(goods.getAuditStatus()))
            .auditNote(goods.getAuditNote())
            .auditTime(goods.getAuditTime())
            .createdAt(goods.getCreatedAt())
            .build();
    }

    @Override
    public GoodsDetailResponse createGoods(Long sellerId, GoodsSaveRequest request) {
        List<String> imageUrls = sanitizeImageUrls(request.getImageUrls());
        if (!StringUtils.hasText(request.getCoverImageUrl()) && !imageUrls.isEmpty()) {
            request.setCoverImageUrl(imageUrls.get(0));
        }

        GoodsEntity goods = new GoodsEntity();
        applyRequest(goods, request);
        goods.setSellerId(sellerId);
        goods.setStatus(STATUS_OFF_SHELF);
        goods.setAuditStatus(AUDIT_PENDING);
        goods.setAuditNote("已提交，待管理员审核");
        goods.setAuditTime(null);
        goodsMapper.insert(goods);
        if (!imageUrls.isEmpty()) {
            replaceGoodsImages(goods.getId(), imageUrls);
        }
        return getGoodsDetailForViewer(goods.getId(), sellerId);
    }

    @Override
    public GoodsDetailResponse updateGoods(Long sellerId, Long goodsId, GoodsSaveRequest request) {
        List<String> imageUrls = sanitizeImageUrls(request.getImageUrls());
        GoodsEntity goods = getSellerOwnedGoods(sellerId, goodsId);
        applyRequest(goods, request);
        if (request.isReplaceImages()) {
            if (imageUrls.isEmpty()) {
                goods.setCoverImageUrl(null);
            } else if (!StringUtils.hasText(goods.getCoverImageUrl()) || !imageUrls.contains(goods.getCoverImageUrl())) {
                goods.setCoverImageUrl(imageUrls.get(0));
            }
        }
        goods.setStatus(STATUS_OFF_SHELF);
        goods.setAuditStatus(AUDIT_PENDING);
        goods.setAuditNote("商品信息已更新，待重新审核");
        goods.setAuditTime(null);
        goodsMapper.updateById(goods);
        if (request.isReplaceImages()) {
            replaceGoodsImages(goods.getId(), imageUrls);
        }
        return getGoodsDetailForViewer(goods.getId(), sellerId);
    }

    @Override
    public void offShelfGoods(Long sellerId, Long goodsId) {
        GoodsEntity goods = getSellerOwnedGoods(sellerId, goodsId);
        goods.setStatus(STATUS_OFF_SHELF);
        goodsMapper.updateById(goods);
    }

    @Override
    public List<GoodsListItemResponse> listSellerGoods(Long sellerId) {
        List<GoodsEntity> goods = goodsMapper.selectList(new LambdaQueryWrapper<GoodsEntity>()
            .eq(GoodsEntity::getSellerId, sellerId)
            .orderByDesc(GoodsEntity::getCreatedAt));
        return toListItems(goods);
    }

    private GoodsEntity getSellerOwnedGoods(Long sellerId, Long goodsId) {
        GoodsEntity goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        if (!sellerId.equals(goods.getSellerId())) {
            throw new BusinessException("无权操作该商品");
        }
        return goods;
    }

    private void applyRequest(GoodsEntity goods, GoodsSaveRequest request) {
        CategoryEntity category = categoryMapper.selectById(request.getCategoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException("商品分类不存在或已停用");
        }

        goods.setCategoryId(request.getCategoryId());
        goods.setTitle(request.getTitle().trim());
        goods.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        goods.setPrice(request.getPrice());
        goods.setConditionLevel(request.getConditionLevel().trim());
        goods.setContactInfo(request.getContactInfo().trim());
        goods.setCoverImageUrl(request.getCoverImageUrl() == null ? null : request.getCoverImageUrl().trim());
    }

    private List<GoodsListItemResponse> toListItems(List<GoodsEntity> goods) {
        List<GoodsListItemResponse> results = new ArrayList<>();
        if (goods.isEmpty()) {
            return results;
        }

        Map<Long, String> categoryNames = new HashMap<>();
        for (CategoryEntity category : categoryMapper.selectList(null)) {
            categoryNames.put(category.getId(), category.getName());
        }

        Set<Long> goodsIds = new LinkedHashSet<>();
        for (GoodsEntity item : goods) {
            goodsIds.add(item.getId());
        }
        Map<Long, String> firstImageMap = buildFirstImageMap(goodsIds);

        for (GoodsEntity item : goods) {
            String coverImageUrl = item.getCoverImageUrl();
            if (!StringUtils.hasText(coverImageUrl)) {
                coverImageUrl = firstImageMap.get(item.getId());
            }
            results.add(GoodsListItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .price(item.getPrice())
                .conditionLevel(item.getConditionLevel())
                .contactInfo(item.getContactInfo())
                .coverImageUrl(coverImageUrl)
                .categoryName(categoryNames.getOrDefault(item.getCategoryId(), "未分类"))
                .status(item.getStatus())
                .auditStatus(item.getAuditStatus())
                .auditStatusLabel(resolveAuditStatusLabel(item.getAuditStatus()))
                .auditNote(item.getAuditNote())
                .auditTime(item.getAuditTime())
                .createdAt(item.getCreatedAt())
                .build());
        }
        return results;
    }

    private List<String> listGoodsImageUrls(Long goodsId) {
        List<GoodsImageEntity> rows = goodsImageMapper.selectList(new LambdaQueryWrapper<GoodsImageEntity>()
            .eq(GoodsImageEntity::getGoodsId, goodsId)
            .orderByAsc(GoodsImageEntity::getSortNo)
            .orderByAsc(GoodsImageEntity::getId));
        List<String> urls = new ArrayList<>();
        for (GoodsImageEntity row : rows) {
            if (StringUtils.hasText(row.getImageUrl())) {
                urls.add(row.getImageUrl());
            }
        }
        return urls;
    }

    private void replaceGoodsImages(Long goodsId, List<String> imageUrls) {
        goodsImageMapper.delete(new LambdaQueryWrapper<GoodsImageEntity>()
            .eq(GoodsImageEntity::getGoodsId, goodsId));
        for (int i = 0; i < imageUrls.size(); i++) {
            GoodsImageEntity image = new GoodsImageEntity();
            image.setGoodsId(goodsId);
            image.setImageUrl(imageUrls.get(i));
            image.setSortNo(i + 1);
            goodsImageMapper.insert(image);
        }
    }

    private Map<Long, String> buildFirstImageMap(Set<Long> goodsIds) {
        Map<Long, String> firstImageMap = new HashMap<>();
        if (goodsIds.isEmpty()) {
            return firstImageMap;
        }
        List<GoodsImageEntity> rows = goodsImageMapper.selectList(new LambdaQueryWrapper<GoodsImageEntity>()
            .in(GoodsImageEntity::getGoodsId, goodsIds)
            .orderByAsc(GoodsImageEntity::getSortNo)
            .orderByAsc(GoodsImageEntity::getId));
        for (GoodsImageEntity row : rows) {
            if (!StringUtils.hasText(row.getImageUrl())) {
                continue;
            }
            if (!firstImageMap.containsKey(row.getGoodsId())) {
                firstImageMap.put(row.getGoodsId(), row.getImageUrl());
            }
        }
        return firstImageMap;
    }

    private List<String> sanitizeImageUrls(List<String> imageUrls) {
        List<String> sanitized = new ArrayList<>();
        if (imageUrls == null || imageUrls.isEmpty()) {
            return sanitized;
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String imageUrl : imageUrls) {
            if (!StringUtils.hasText(imageUrl)) {
                continue;
            }
            unique.add(imageUrl.trim());
        }
        sanitized.addAll(unique);
        if (sanitized.size() > MAX_IMAGE_COUNT) {
            throw new BusinessException("商品图片最多上传 " + MAX_IMAGE_COUNT + " 张");
        }
        return sanitized;
    }

    private String resolveAuditStatusLabel(String status) {
        if (AUDIT_PENDING.equals(status)) {
            return "待审核";
        }
        if (AUDIT_APPROVED.equals(status)) {
            return "已通过";
        }
        if (AUDIT_REJECTED.equals(status)) {
            return "已驳回";
        }
        return "未知";
    }
}

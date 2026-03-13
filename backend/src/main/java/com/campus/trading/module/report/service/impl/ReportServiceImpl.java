package com.campus.trading.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.goods.entity.GoodsEntity;
import com.campus.trading.module.goods.mapper.GoodsMapper;
import com.campus.trading.module.order.entity.TradeOrderEntity;
import com.campus.trading.module.order.mapper.TradeOrderMapper;
import com.campus.trading.module.report.dto.ReportResponse;
import com.campus.trading.module.report.entity.ReportEntity;
import com.campus.trading.module.report.mapper.ReportMapper;
import com.campus.trading.module.report.service.ReportService;
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
public class ReportServiceImpl implements ReportService {

    private static final String TARGET_TYPE_GOODS = "GOODS";
    private static final String TARGET_TYPE_USER = "USER";
    private static final String TARGET_TYPE_ORDER = "ORDER";

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_RESOLVED = "RESOLVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final ReportMapper reportMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final TradeOrderMapper tradeOrderMapper;

    public ReportServiceImpl(ReportMapper reportMapper,
                             GoodsMapper goodsMapper,
                             UserMapper userMapper,
                             TradeOrderMapper tradeOrderMapper) {
        this.reportMapper = reportMapper;
        this.goodsMapper = goodsMapper;
        this.userMapper = userMapper;
        this.tradeOrderMapper = tradeOrderMapper;
    }

    @Override
    public Long createReport(Long reporterId, String targetType, Long targetId, String reason, String detail) {
        String normalizedTargetType = normalizeTargetType(targetType);
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("举报目标不合法");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("请填写举报原因");
        }
        if (TARGET_TYPE_USER.equals(normalizedTargetType) && reporterId.equals(targetId)) {
            throw new BusinessException("不能举报自己");
        }

        validateTarget(normalizedTargetType, targetId);

        ReportEntity report = new ReportEntity();
        report.setReporterId(reporterId);
        report.setTargetType(normalizedTargetType);
        report.setTargetId(targetId);
        report.setReason(limit(reason.trim(), 128));
        report.setDetail(normalizeOptional(detail, 500));
        report.setStatus(STATUS_PENDING);
        reportMapper.insert(report);
        return report.getId();
    }

    @Override
    public List<ReportResponse> listUserReports(Long reporterId) {
        List<ReportEntity> reports = reportMapper.selectList(new LambdaQueryWrapper<ReportEntity>()
            .eq(ReportEntity::getReporterId, reporterId)
            .orderByDesc(ReportEntity::getCreatedAt));
        return toResponses(reports);
    }

    @Override
    public List<ReportResponse> listReports(String status, String targetType) {
        LambdaQueryWrapper<ReportEntity> wrapper = new LambdaQueryWrapper<ReportEntity>()
            .orderByDesc(ReportEntity::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(ReportEntity::getStatus, status.trim().toUpperCase());
        }
        if (StringUtils.hasText(targetType)) {
            wrapper.eq(ReportEntity::getTargetType, targetType.trim().toUpperCase());
        }
        return toResponses(reportMapper.selectList(wrapper));
    }

    @Override
    public void processReport(Long reportId, String status, String handleNote) {
        ReportEntity report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        String targetStatus = status == null ? "" : status.trim().toUpperCase();
        if (!STATUS_PROCESSING.equals(targetStatus)
            && !STATUS_RESOLVED.equals(targetStatus)
            && !STATUS_REJECTED.equals(targetStatus)) {
            throw new BusinessException("处理状态不合法");
        }
        report.setStatus(targetStatus);
        report.setHandleNote(normalizeOptional(handleNote, 500));
        report.setHandledAt(LocalDateTime.now());
        reportMapper.updateById(report);
    }

    private List<ReportResponse> toResponses(List<ReportEntity> reports) {
        List<ReportResponse> result = new ArrayList<>();
        if (reports == null || reports.isEmpty()) {
            return result;
        }

        Set<Long> reporterIds = new LinkedHashSet<>();
        Set<Long> goodsIds = new LinkedHashSet<>();
        Set<Long> userIds = new LinkedHashSet<>();
        Set<Long> orderIds = new LinkedHashSet<>();
        for (ReportEntity report : reports) {
            reporterIds.add(report.getReporterId());
            if (TARGET_TYPE_GOODS.equals(report.getTargetType())) {
                goodsIds.add(report.getTargetId());
            } else if (TARGET_TYPE_USER.equals(report.getTargetType())) {
                userIds.add(report.getTargetId());
            } else if (TARGET_TYPE_ORDER.equals(report.getTargetType())) {
                orderIds.add(report.getTargetId());
            }
        }

        Map<Long, UserEntity> reporterMap = new HashMap<>();
        for (UserEntity user : userMapper.selectBatchIds(reporterIds)) {
            reporterMap.put(user.getId(), user);
        }

        Map<Long, GoodsEntity> goodsMap = new HashMap<>();
        for (GoodsEntity goods : goodsMapper.selectBatchIds(goodsIds)) {
            goodsMap.put(goods.getId(), goods);
        }

        Map<Long, UserEntity> userMap = new HashMap<>();
        for (UserEntity user : userMapper.selectBatchIds(userIds)) {
            userMap.put(user.getId(), user);
        }

        Map<Long, TradeOrderEntity> orderMap = new HashMap<>();
        for (TradeOrderEntity order : tradeOrderMapper.selectBatchIds(orderIds)) {
            orderMap.put(order.getId(), order);
        }

        for (ReportEntity report : reports) {
            UserEntity reporter = reporterMap.get(report.getReporterId());
            result.add(ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .reporterName(resolveDisplayName(reporter))
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .targetSummary(resolveTargetSummary(report, goodsMap, userMap, orderMap))
                .reason(report.getReason())
                .detail(report.getDetail())
                .status(report.getStatus())
                .statusLabel(resolveStatusLabel(report.getStatus()))
                .handleNote(report.getHandleNote())
                .handledAt(report.getHandledAt())
                .createdAt(report.getCreatedAt())
                .build());
        }
        return result;
    }

    private void validateTarget(String targetType, Long targetId) {
        if (TARGET_TYPE_GOODS.equals(targetType)) {
            if (goodsMapper.selectById(targetId) == null) {
                throw new BusinessException("举报商品不存在");
            }
            return;
        }
        if (TARGET_TYPE_USER.equals(targetType)) {
            if (userMapper.selectById(targetId) == null) {
                throw new BusinessException("举报用户不存在");
            }
            return;
        }
        if (TARGET_TYPE_ORDER.equals(targetType)) {
            if (tradeOrderMapper.selectById(targetId) == null) {
                throw new BusinessException("举报订单不存在");
            }
            return;
        }
        throw new BusinessException("举报目标类型不支持");
    }

    private String normalizeTargetType(String targetType) {
        if (!StringUtils.hasText(targetType)) {
            throw new BusinessException("举报目标类型不能为空");
        }
        return targetType.trim().toUpperCase();
    }

    private String normalizeOptional(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return limit(trimmed, maxLength);
    }

    private String limit(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String resolveTargetSummary(ReportEntity report,
                                        Map<Long, GoodsEntity> goodsMap,
                                        Map<Long, UserEntity> userMap,
                                        Map<Long, TradeOrderEntity> orderMap) {
        if (TARGET_TYPE_GOODS.equals(report.getTargetType())) {
            GoodsEntity goods = goodsMap.get(report.getTargetId());
            return goods == null ? "商品已删除" : goods.getTitle();
        }
        if (TARGET_TYPE_USER.equals(report.getTargetType())) {
            UserEntity user = userMap.get(report.getTargetId());
            return user == null ? "用户不存在" : resolveDisplayName(user);
        }
        if (TARGET_TYPE_ORDER.equals(report.getTargetType())) {
            TradeOrderEntity order = orderMap.get(report.getTargetId());
            return order == null ? "订单不存在" : ("订单号 " + order.getOrderNo());
        }
        return "未知目标";
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

    private String resolveStatusLabel(String status) {
        if (STATUS_PENDING.equals(status)) {
            return "待处理";
        }
        if (STATUS_PROCESSING.equals(status)) {
            return "处理中";
        }
        if (STATUS_RESOLVED.equals(status)) {
            return "已处理";
        }
        if (STATUS_REJECTED.equals(status)) {
            return "已驳回";
        }
        return status;
    }
}

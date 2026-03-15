package com.campus.trading.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.cart.entity.CartEntity;
import com.campus.trading.module.cart.mapper.CartMapper;
import com.campus.trading.module.goods.entity.GoodsEntity;
import com.campus.trading.module.goods.mapper.GoodsMapper;
import com.campus.trading.module.order.dto.OrderItemResponse;
import com.campus.trading.module.order.dto.OrderLogItemResponse;
import com.campus.trading.module.order.entity.OrderLogEntity;
import com.campus.trading.module.order.entity.TradeOrderEntity;
import com.campus.trading.module.order.mapper.OrderLogMapper;
import com.campus.trading.module.order.mapper.TradeOrderMapper;
import com.campus.trading.module.order.service.TradeOrderService;
import com.campus.trading.module.user.entity.UserEntity;
import com.campus.trading.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TradeOrderServiceImpl implements TradeOrderService {

    private static final String GOODS_STATUS_ON_SHELF = "ON_SHELF";
    private static final String GOODS_STATUS_OFF_SHELF = "OFF_SHELF";
    private static final String ORDER_STATUS_PENDING = "PENDING";
    private static final String ORDER_STATUS_SELLER_CONFIRMED = "SELLER_CONFIRMED";
    private static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    private final TradeOrderMapper tradeOrderMapper;
    private final OrderLogMapper orderLogMapper;
    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;

    public TradeOrderServiceImpl(TradeOrderMapper tradeOrderMapper,
                                 OrderLogMapper orderLogMapper,
                                 CartMapper cartMapper,
                                 GoodsMapper goodsMapper,
                                 UserMapper userMapper) {
        this.tradeOrderMapper = tradeOrderMapper;
        this.orderLogMapper = orderLogMapper;
        this.cartMapper = cartMapper;
        this.goodsMapper = goodsMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkout(Long buyerId,
                        String buyerRemark,
                        LocalDateTime meetupTime,
                        String meetupLocation,
                        String meetupNote) {
        List<CartEntity> cartItems = cartMapper.selectList(new LambdaQueryWrapper<CartEntity>()
            .eq(CartEntity::getUserId, buyerId)
            .orderByAsc(CartEntity::getCreatedAt));
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车为空，无法结算");
        }

        Set<Long> goodsIds = new LinkedHashSet<>();
        for (CartEntity cartItem : cartItems) {
            goodsIds.add(cartItem.getGoodsId());
        }
        List<GoodsEntity> goodsList = goodsMapper.selectBatchIds(goodsIds);
        Map<Long, GoodsEntity> goodsMap = new HashMap<>();
        for (GoodsEntity goods : goodsList) {
            goodsMap.put(goods.getId(), goods);
        }

        String remark = sanitizeRemark(buyerRemark);
        String normalizedMeetupLocation = sanitizeMeetupLocation(meetupLocation);
        String normalizedMeetupNote = sanitizeMeetupNote(meetupNote);
        int created = 0;
        for (CartEntity cartItem : cartItems) {
            GoodsEntity goods = goodsMap.get(cartItem.getGoodsId());
            if (goods == null) {
                throw new BusinessException("购物车中存在已删除商品，请刷新后重试");
            }
            if (!GOODS_STATUS_ON_SHELF.equals(goods.getStatus())) {
                throw new BusinessException("商品“" + goods.getTitle() + "”当前不可下单");
            }
            if (buyerId.equals(goods.getSellerId())) {
                throw new BusinessException("不能购买自己发布的商品");
            }

            int updateCount = goodsMapper.update(null, new LambdaUpdateWrapper<GoodsEntity>()
                .set(GoodsEntity::getStatus, GOODS_STATUS_OFF_SHELF)
                .eq(GoodsEntity::getId, goods.getId())
                .eq(GoodsEntity::getStatus, GOODS_STATUS_ON_SHELF));
            if (updateCount != 1) {
                throw new BusinessException("商品“" + goods.getTitle() + "”已被其他用户下单");
            }

            TradeOrderEntity order = new TradeOrderEntity();
            order.setOrderNo(generateOrderNo());
            order.setGoodsId(goods.getId());
            order.setBuyerId(buyerId);
            order.setSellerId(goods.getSellerId());
            order.setAmount(goods.getPrice() == null ? BigDecimal.ZERO : goods.getPrice());
            order.setBuyerRemark(remark);
            order.setMeetupTime(meetupTime);
            order.setMeetupLocation(normalizedMeetupLocation);
            order.setMeetupNote(normalizedMeetupNote);
            order.setStatus(ORDER_STATUS_PENDING);
            tradeOrderMapper.insert(order);
            appendOrderLog(order.getId(), buyerId, "CREATE", null, ORDER_STATUS_PENDING,
                buildCreateOrderNote(normalizedMeetupLocation, meetupTime));
            created++;
        }

        cartMapper.delete(new LambdaQueryWrapper<CartEntity>()
            .eq(CartEntity::getUserId, buyerId));
        return created;
    }

    @Override
    public List<OrderItemResponse> listUserOrders(Long userId) {
        List<TradeOrderEntity> orders = tradeOrderMapper.selectList(new LambdaQueryWrapper<TradeOrderEntity>()
            .and(w -> w.eq(TradeOrderEntity::getBuyerId, userId).or().eq(TradeOrderEntity::getSellerId, userId))
            .orderByDesc(TradeOrderEntity::getCreatedAt));
        return toOrderItems(orders, userId);
    }

    @Override
    public List<OrderItemResponse> listBuyerOrders(Long buyerId) {
        List<TradeOrderEntity> orders = tradeOrderMapper.selectList(new LambdaQueryWrapper<TradeOrderEntity>()
            .eq(TradeOrderEntity::getBuyerId, buyerId)
            .orderByDesc(TradeOrderEntity::getCreatedAt));
        return toOrderItems(orders, buyerId);
    }

    @Override
    public List<OrderItemResponse> listSellerOrders(Long sellerId) {
        List<TradeOrderEntity> orders = tradeOrderMapper.selectList(new LambdaQueryWrapper<TradeOrderEntity>()
            .eq(TradeOrderEntity::getSellerId, sellerId)
            .orderByDesc(TradeOrderEntity::getCreatedAt));
        return toOrderItems(orders, sellerId);
    }

    private List<OrderItemResponse> toOrderItems(List<TradeOrderEntity> orders, Long currentUserId) {
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, GoodsEntity> goodsMap = buildGoodsMap(orders);
        Map<Long, UserEntity> userMap = buildUserMap(orders);

        List<OrderItemResponse> results = new ArrayList<>();
        for (TradeOrderEntity order : orders) {
            results.add(toOrderItem(order, currentUserId, goodsMap, userMap));
        }
        return results;
    }

    @Override
    public OrderItemResponse getUserOrder(Long userId, Long orderId) {
        TradeOrderEntity order = getAccessibleOrderOrThrow(userId, orderId);
        List<TradeOrderEntity> wrapper = List.of(order);
        Map<Long, GoodsEntity> goodsMap = buildGoodsMap(wrapper);
        Map<Long, UserEntity> userMap = buildUserMap(wrapper);
        return toOrderItem(order, userId, goodsMap, userMap);
    }

    @Override
    public List<OrderLogItemResponse> listOrderLogs(Long userId, Long orderId) {
        TradeOrderEntity order = getAccessibleOrderOrThrow(userId, orderId);
        List<OrderLogEntity> logs = orderLogMapper.selectList(new LambdaQueryWrapper<OrderLogEntity>()
            .eq(OrderLogEntity::getOrderId, order.getId())
            .orderByAsc(OrderLogEntity::getCreatedAt)
            .orderByAsc(OrderLogEntity::getId));
        if (logs.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> operatorIds = new LinkedHashSet<>();
        for (OrderLogEntity log : logs) {
            operatorIds.add(log.getOperatorUserId());
        }

        Map<Long, UserEntity> userMap = new HashMap<>();
        if (!operatorIds.isEmpty()) {
            for (UserEntity user : userMapper.selectBatchIds(operatorIds)) {
                userMap.put(user.getId(), user);
            }
        }

        List<OrderLogItemResponse> results = new ArrayList<>();
        for (OrderLogEntity log : logs) {
            String fromStatusLabel = log.getFromStatus() == null ? "初始状态" : resolveStatusLabel(log.getFromStatus());
            results.add(OrderLogItemResponse.builder()
                .id(log.getId())
                .orderId(log.getOrderId())
                .action(log.getAction())
                .actionLabel(resolveActionLabel(log.getAction()))
                .fromStatus(log.getFromStatus())
                .fromStatusLabel(fromStatusLabel)
                .toStatus(log.getToStatus())
                .toStatusLabel(resolveStatusLabel(log.getToStatus()))
                .operatorUserId(log.getOperatorUserId())
                .operatorName(resolveDisplayName(userMap.get(log.getOperatorUserId())))
                .note(log.getNote())
                .createdAt(log.getCreatedAt())
                .build());
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMeetup(Long userId,
                             Long orderId,
                             LocalDateTime meetupTime,
                             String meetupLocation,
                             String meetupNote) {
        TradeOrderEntity order = getAccessibleOrderOrThrow(userId, orderId);
        if (ORDER_STATUS_COMPLETED.equals(order.getStatus()) || ORDER_STATUS_CANCELLED.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可修改见面信息");
        }

        String normalizedMeetupLocation = sanitizeMeetupLocation(meetupLocation);
        String normalizedMeetupNote = sanitizeMeetupNote(meetupNote);
        boolean changed = !Objects.equals(order.getMeetupTime(), meetupTime)
            || !Objects.equals(order.getMeetupLocation(), normalizedMeetupLocation)
            || !Objects.equals(order.getMeetupNote(), normalizedMeetupNote);

        if (!changed) {
            return;
        }

        order.setMeetupTime(meetupTime);
        order.setMeetupLocation(normalizedMeetupLocation);
        order.setMeetupNote(normalizedMeetupNote);
        tradeOrderMapper.updateById(order);

        String note = "更新见面信息";
        if (StringUtils.hasText(normalizedMeetupLocation)) {
            note += "，地点：" + normalizedMeetupLocation;
        }
        if (meetupTime != null) {
            note += "，时间：" + meetupTime;
        }
        appendOrderLog(order.getId(), userId, "MEETUP_UPDATE", order.getStatus(), order.getStatus(), note);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long buyerId, Long orderId) {
        TradeOrderEntity order = getOrderOrThrow(orderId);
        if (!buyerId.equals(order.getBuyerId())) {
            throw new BusinessException("无权取消该订单");
        }
        if (!ORDER_STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可取消");
        }

        String fromStatus = order.getStatus();
        order.setStatus(ORDER_STATUS_CANCELLED);
        tradeOrderMapper.updateById(order);
        appendOrderLog(order.getId(), buyerId, "CANCEL", fromStatus, ORDER_STATUS_CANCELLED, "买家取消订单");

        goodsMapper.update(null, new LambdaUpdateWrapper<GoodsEntity>()
            .set(GoodsEntity::getStatus, GOODS_STATUS_ON_SHELF)
            .eq(GoodsEntity::getId, order.getGoodsId())
            .eq(GoodsEntity::getStatus, GOODS_STATUS_OFF_SHELF));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sellerConfirm(Long sellerId, Long orderId) {
        TradeOrderEntity order = getOrderOrThrow(orderId);
        if (!sellerId.equals(order.getSellerId())) {
            throw new BusinessException("无权操作该订单");
        }
        if (!ORDER_STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可确认");
        }
        String fromStatus = order.getStatus();
        order.setStatus(ORDER_STATUS_SELLER_CONFIRMED);
        tradeOrderMapper.updateById(order);
        appendOrderLog(order.getId(), sellerId, "SELLER_CONFIRM", fromStatus, ORDER_STATUS_SELLER_CONFIRMED, "卖家确认接单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyerComplete(Long buyerId, Long orderId) {
        TradeOrderEntity order = getOrderOrThrow(orderId);
        if (!buyerId.equals(order.getBuyerId())) {
            throw new BusinessException("无权操作该订单");
        }
        if (!ORDER_STATUS_SELLER_CONFIRMED.equals(order.getStatus())) {
            throw new BusinessException("请等待卖家确认后再完成订单");
        }
        String fromStatus = order.getStatus();
        order.setStatus(ORDER_STATUS_COMPLETED);
        tradeOrderMapper.updateById(order);
        appendOrderLog(order.getId(), buyerId, "BUYER_COMPLETE", fromStatus, ORDER_STATUS_COMPLETED, "买家确认完成交易");
    }

    private TradeOrderEntity getOrderOrThrow(Long orderId) {
        TradeOrderEntity order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private TradeOrderEntity getAccessibleOrderOrThrow(Long userId, Long orderId) {
        TradeOrderEntity order = getOrderOrThrow(orderId);
        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getSellerId())) {
            throw new BusinessException("订单不存在或无权访问");
        }
        return order;
    }

    private Map<Long, GoodsEntity> buildGoodsMap(List<TradeOrderEntity> orders) {
        Set<Long> goodsIds = new LinkedHashSet<>();
        for (TradeOrderEntity order : orders) {
            goodsIds.add(order.getGoodsId());
        }
        Map<Long, GoodsEntity> goodsMap = new HashMap<>();
        for (GoodsEntity goods : goodsMapper.selectBatchIds(goodsIds)) {
            goodsMap.put(goods.getId(), goods);
        }
        return goodsMap;
    }

    private Map<Long, UserEntity> buildUserMap(List<TradeOrderEntity> orders) {
        Set<Long> userIds = new LinkedHashSet<>();
        for (TradeOrderEntity order : orders) {
            userIds.add(order.getBuyerId());
            userIds.add(order.getSellerId());
        }
        Map<Long, UserEntity> userMap = new HashMap<>();
        for (UserEntity user : userMapper.selectBatchIds(userIds)) {
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    private OrderItemResponse toOrderItem(TradeOrderEntity order,
                                          Long currentUserId,
                                          Map<Long, GoodsEntity> goodsMap,
                                          Map<Long, UserEntity> userMap) {
        GoodsEntity goods = goodsMap.get(order.getGoodsId());
        UserEntity buyer = userMap.get(order.getBuyerId());
        UserEntity seller = userMap.get(order.getSellerId());
        return OrderItemResponse.builder()
            .orderId(order.getId())
            .orderNo(order.getOrderNo())
            .goodsId(order.getGoodsId())
            .goodsTitle(goods == null ? "商品已删除" : goods.getTitle())
            .goodsCoverImageUrl(goods == null ? null : goods.getCoverImageUrl())
            .amount(order.getAmount())
            .status(order.getStatus())
            .statusLabel(resolveStatusLabel(order.getStatus()))
            .buyerRemark(order.getBuyerRemark())
            .meetupTime(order.getMeetupTime())
            .meetupLocation(order.getMeetupLocation())
            .meetupNote(order.getMeetupNote())
            .buyerId(order.getBuyerId())
            .buyerName(resolveDisplayName(buyer))
            .sellerId(order.getSellerId())
            .sellerName(resolveDisplayName(seller))
            .buyerSide(currentUserId.equals(order.getBuyerId()))
            .sellerSide(currentUserId.equals(order.getSellerId()))
            .createdAt(order.getCreatedAt())
            .build();
    }

    private String sanitizeRemark(String buyerRemark) {
        if (!StringUtils.hasText(buyerRemark)) {
            return null;
        }
        String normalized = buyerRemark.trim();
        if (normalized.length() <= 255) {
            return normalized;
        }
        return normalized.substring(0, 255);
    }

    private String generateOrderNo() {
        long millis = System.currentTimeMillis();
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "OD" + millis + suffix;
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

    private String resolveActionLabel(String action) {
        if ("CREATE".equals(action)) {
            return "创建订单";
        }
        if ("CANCEL".equals(action)) {
            return "取消订单";
        }
        if ("SELLER_CONFIRM".equals(action)) {
            return "卖家确认";
        }
        if ("BUYER_COMPLETE".equals(action)) {
            return "买家确认收货";
        }
        if ("MEETUP_UPDATE".equals(action)) {
            return "更新见面信息";
        }
        return "状态流转";
    }

    private void appendOrderLog(Long orderId,
                                Long operatorUserId,
                                String action,
                                String fromStatus,
                                String toStatus,
                                String note) {
        OrderLogEntity log = new OrderLogEntity();
        log.setOrderId(orderId);
        log.setOperatorUserId(operatorUserId);
        log.setAction(action);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setNote(note);
        orderLogMapper.insert(log);
    }

    private String sanitizeMeetupLocation(String meetupLocation) {
        if (!StringUtils.hasText(meetupLocation)) {
            return null;
        }
        String normalized = meetupLocation.trim();
        if (normalized.length() <= 255) {
            return normalized;
        }
        return normalized.substring(0, 255);
    }

    private String sanitizeMeetupNote(String meetupNote) {
        if (!StringUtils.hasText(meetupNote)) {
            return null;
        }
        String normalized = meetupNote.trim();
        if (normalized.length() <= 255) {
            return normalized;
        }
        return normalized.substring(0, 255);
    }

    private String buildCreateOrderNote(String meetupLocation, LocalDateTime meetupTime) {
        if (!StringUtils.hasText(meetupLocation) && meetupTime == null) {
            return "买家提交订单";
        }
        StringBuilder builder = new StringBuilder("买家提交订单");
        if (StringUtils.hasText(meetupLocation)) {
            builder.append("，地点：").append(meetupLocation);
        }
        if (meetupTime != null) {
            builder.append("，时间：").append(meetupTime);
        }
        return builder.toString();
    }
}

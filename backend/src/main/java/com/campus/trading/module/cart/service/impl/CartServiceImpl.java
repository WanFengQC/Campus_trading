package com.campus.trading.module.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.cart.dto.CartItemResponse;
import com.campus.trading.module.cart.entity.CartEntity;
import com.campus.trading.module.cart.mapper.CartMapper;
import com.campus.trading.module.cart.service.CartService;
import com.campus.trading.module.goods.entity.GoodsEntity;
import com.campus.trading.module.goods.mapper.GoodsMapper;
import com.campus.trading.module.user.entity.UserEntity;
import com.campus.trading.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CartServiceImpl implements CartService {

    private static final String GOODS_STATUS_ON_SHELF = "ON_SHELF";

    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;

    public CartServiceImpl(CartMapper cartMapper,
                           GoodsMapper goodsMapper,
                           UserMapper userMapper) {
        this.cartMapper = cartMapper;
        this.goodsMapper = goodsMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void addItem(Long userId, Long goodsId) {
        GoodsEntity goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        if (!GOODS_STATUS_ON_SHELF.equals(goods.getStatus())) {
            throw new BusinessException("该商品当前不可加入购物车");
        }
        if (userId.equals(goods.getSellerId())) {
            throw new BusinessException("不能购买自己发布的商品");
        }

        CartEntity existing = cartMapper.selectOne(new LambdaQueryWrapper<CartEntity>()
            .eq(CartEntity::getUserId, userId)
            .eq(CartEntity::getGoodsId, goodsId)
            .last("limit 1"));
        if (existing != null) {
            throw new BusinessException("该商品已在购物车中");
        }

        CartEntity cart = new CartEntity();
        cart.setUserId(userId);
        cart.setGoodsId(goodsId);
        cartMapper.insert(cart);
    }

    @Override
    public void removeItem(Long userId, Long cartId) {
        CartEntity cart = cartMapper.selectById(cartId);
        if (cart == null || !userId.equals(cart.getUserId())) {
            throw new BusinessException("购物车记录不存在");
        }
        cartMapper.deleteById(cartId);
    }

    @Override
    public List<CartItemResponse> listUserCart(Long userId) {
        List<CartEntity> cartList = cartMapper.selectList(new LambdaQueryWrapper<CartEntity>()
            .eq(CartEntity::getUserId, userId)
            .orderByDesc(CartEntity::getCreatedAt));
        if (cartList.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> goodsIds = new LinkedHashSet<>();
        for (CartEntity cart : cartList) {
            goodsIds.add(cart.getGoodsId());
        }

        Map<Long, GoodsEntity> goodsMap = new HashMap<>();
        Set<Long> sellerIds = new LinkedHashSet<>();
        List<GoodsEntity> goodsList = goodsMapper.selectBatchIds(goodsIds);
        for (GoodsEntity goods : goodsList) {
            goodsMap.put(goods.getId(), goods);
            sellerIds.add(goods.getSellerId());
        }

        Map<Long, UserEntity> sellerMap = new HashMap<>();
        if (!sellerIds.isEmpty()) {
            List<UserEntity> sellers = userMapper.selectBatchIds(sellerIds);
            for (UserEntity seller : sellers) {
                sellerMap.put(seller.getId(), seller);
            }
        }

        List<CartItemResponse> results = new ArrayList<>();
        for (CartEntity cart : cartList) {
            GoodsEntity goods = goodsMap.get(cart.getGoodsId());
            if (goods == null) {
                continue;
            }
            UserEntity seller = sellerMap.get(goods.getSellerId());

            results.add(CartItemResponse.builder()
                .cartId(cart.getId())
                .goodsId(goods.getId())
                .goodsTitle(goods.getTitle())
                .goodsCoverImageUrl(goods.getCoverImageUrl())
                .goodsConditionLevel(goods.getConditionLevel())
                .goodsStatus(goods.getStatus())
                .sellerName(resolveDisplayName(seller))
                .contactInfo(goods.getContactInfo())
                .price(goods.getPrice())
                .addedAt(cart.getCreatedAt())
                .build());
        }
        return results;
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
}

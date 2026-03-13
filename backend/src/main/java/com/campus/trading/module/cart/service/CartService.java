package com.campus.trading.module.cart.service;

import com.campus.trading.module.cart.dto.CartItemResponse;

import java.util.List;

public interface CartService {

    void addItem(Long userId, Long goodsId);

    void removeItem(Long userId, Long cartId);

    List<CartItemResponse> listUserCart(Long userId);
}

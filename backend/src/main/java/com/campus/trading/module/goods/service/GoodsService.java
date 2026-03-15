package com.campus.trading.module.goods.service;

import com.campus.trading.module.goods.dto.GoodsDetailResponse;
import com.campus.trading.module.goods.dto.GoodsListItemResponse;
import com.campus.trading.module.goods.dto.GoodsQueryRequest;
import com.campus.trading.module.goods.dto.GoodsSaveRequest;

import java.util.List;

public interface GoodsService {

    List<GoodsListItemResponse> searchOnShelfGoods(GoodsQueryRequest request);

    GoodsDetailResponse getGoodsDetail(Long goodsId);

    GoodsDetailResponse getGoodsDetailForViewer(Long goodsId, Long viewerUserId);

    GoodsDetailResponse getGoodsDetailForAdmin(Long goodsId);

    GoodsDetailResponse createGoods(Long sellerId, GoodsSaveRequest request);

    GoodsDetailResponse updateGoods(Long sellerId, Long goodsId, GoodsSaveRequest request);

    void offShelfGoods(Long sellerId, Long goodsId);

    List<GoodsListItemResponse> listSellerGoods(Long sellerId);
}

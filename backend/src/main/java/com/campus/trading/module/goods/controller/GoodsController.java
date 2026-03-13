package com.campus.trading.module.goods.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.module.goods.dto.GoodsDetailResponse;
import com.campus.trading.module.goods.dto.GoodsListItemResponse;
import com.campus.trading.module.goods.dto.GoodsQueryRequest;
import com.campus.trading.module.goods.service.GoodsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping
    public ApiResponse<List<GoodsListItemResponse>> list(String keyword,
                                                         Long categoryId,
                                                         String conditionLevel,
                                                         BigDecimal minPrice,
                                                         BigDecimal maxPrice) {
        GoodsQueryRequest request = new GoodsQueryRequest();
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setConditionLevel(conditionLevel);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        return ApiResponse.success(goodsService.searchOnShelfGoods(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<GoodsDetailResponse> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(goodsService.getGoodsDetail(id));
    }
}
package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.browse.service.BrowseHistoryService;
import com.campus.trading.module.category.service.CategoryService;
import com.campus.trading.module.favorite.service.FavoriteService;
import com.campus.trading.module.file.dto.FileUploadResponse;
import com.campus.trading.module.file.service.FileStorageService;
import com.campus.trading.module.goods.dto.GoodsDetailResponse;
import com.campus.trading.module.goods.dto.GoodsQueryRequest;
import com.campus.trading.module.goods.dto.GoodsSaveRequest;
import com.campus.trading.module.goods.service.GoodsService;
import com.campus.trading.module.order.dto.OrderItemResponse;
import com.campus.trading.module.order.service.TradeOrderService;
import com.campus.trading.module.review.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Controller
public class WebGoodsController {

    private final GoodsService goodsService;
    private final CategoryService categoryService;
    private final FavoriteService favoriteService;
    private final BrowseHistoryService browseHistoryService;
    private final FileStorageService fileStorageService;
    private final ReviewService reviewService;
    private final TradeOrderService tradeOrderService;

    public WebGoodsController(GoodsService goodsService,
                              CategoryService categoryService,
                              FavoriteService favoriteService,
                              BrowseHistoryService browseHistoryService,
                              FileStorageService fileStorageService,
                              ReviewService reviewService,
                              TradeOrderService tradeOrderService) {
        this.goodsService = goodsService;
        this.categoryService = categoryService;
        this.favoriteService = favoriteService;
        this.browseHistoryService = browseHistoryService;
        this.fileStorageService = fileStorageService;
        this.reviewService = reviewService;
        this.tradeOrderService = tradeOrderService;
    }

    @GetMapping("/goods")
    public String goodsList(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "categoryId", required = false) Long categoryId,
                            @RequestParam(value = "conditionLevel", required = false) String conditionLevel,
                            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                            Model model,
                            HttpSession session) {
        GoodsQueryRequest request = new GoodsQueryRequest();
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setConditionLevel(conditionLevel);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);

        Long userId = getLoginUserId(session);
        Set<Long> favoriteGoodsIds = userId == null
            ? Collections.emptySet()
            : favoriteService.listFavoriteGoodsIds(userId);

        model.addAttribute("categories", categoryService.listActiveCategories());
        model.addAttribute("items", goodsService.searchOnShelfGoods(request));
        model.addAttribute("favoriteGoodsIds", favoriteGoodsIds);
        model.addAttribute("loggedIn", userId != null);
        model.addAttribute("q", request);
        return "pages/goods-list";
    }

    @GetMapping("/goods/{id}")
    public String goodsDetail(@PathVariable("id") Long id,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        boolean adminView = getAdminId(session) != null;
        try {
            GoodsDetailResponse goods = adminView
                ? goodsService.getGoodsDetailForAdmin(id)
                : goodsService.getGoodsDetailForViewer(id, userId);
            if (userId != null && !adminView) {
                browseHistoryService.recordView(userId, id);
            }
            model.addAttribute("goods", goods);
            model.addAttribute("reviews", reviewService.listGoodsReviews(id));
            model.addAttribute("loggedIn", userId != null);
            model.addAttribute("adminView", adminView);
            model.addAttribute("favorited", userId != null && favoriteService.isFavorited(userId, id));
            model.addAttribute("sellerView", userId != null && userId.equals(goods.getSellerId()));
            return "pages/goods-detail";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/goods";
        }
    }

    @GetMapping("/goods/publish")
    public String publishPage(Model model, HttpSession session) {
        if (getLoginUserId(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("categories", categoryService.listActiveCategories());
        return "pages/goods-publish";
    }

    @PostMapping("/goods/publish")
    public String publish(@RequestParam("categoryId") Long categoryId,
                          @RequestParam("title") String title,
                          @RequestParam(value = "description", required = false) String description,
                          @RequestParam("price") BigDecimal price,
                          @RequestParam("conditionLevel") String conditionLevel,
                          @RequestParam("contactInfo") String contactInfo,
                          @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            List<String> imageUrls = uploadGoodsImagesIfPresent(imageFiles, userId);
            GoodsSaveRequest request = new GoodsSaveRequest();
            request.setCategoryId(categoryId);
            request.setTitle(title);
            request.setDescription(description);
            request.setPrice(price);
            request.setConditionLevel(conditionLevel);
            request.setContactInfo(contactInfo);
            request.setCoverImageUrl(imageUrls.isEmpty() ? null : imageUrls.get(0));
            request.setImageUrls(imageUrls);
            request.setReplaceImages(!imageUrls.isEmpty());
            GoodsDetailResponse response = goodsService.createGoods(userId, request);
            redirectAttributes.addFlashAttribute("successMessage", "商品已提交审核，审核通过后自动上架");
            return "redirect:/goods/" + response.getId();
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/goods/publish";
        }
    }

    @GetMapping("/goods/my")
    public String myGoods(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        List<OrderItemResponse> sellerOrders = tradeOrderService.listSellerOrders(userId);
        Set<Long> soldGoodsIds = new LinkedHashSet<>();
        for (OrderItemResponse order : sellerOrders) {
            if (order.getGoodsId() != null && !"CANCELLED".equals(order.getStatus())) {
                soldGoodsIds.add(order.getGoodsId());
            }
        }
        model.addAttribute("items", goodsService.listSellerGoods(userId));
        model.addAttribute("sellerOrders", sellerOrders);
        model.addAttribute("soldGoodsIds", soldGoodsIds);
        model.addAttribute("loggedIn", true);
        return "pages/goods-my";
    }

    @GetMapping("/goods/{id}/edit")
    public String editPage(@PathVariable("id") Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        GoodsDetailResponse goods;
        try {
            goods = goodsService.getGoodsDetailForViewer(id, userId);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/goods/my";
        }

        if (!userId.equals(goods.getSellerId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "无权编辑该商品");
            return "redirect:/goods/my";
        }

        model.addAttribute("categories", categoryService.listActiveCategories());
        model.addAttribute("goods", goods);
        model.addAttribute("loggedIn", true);
        return "pages/goods-edit";
    }

    @PostMapping("/goods/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @RequestParam("categoryId") Long categoryId,
                       @RequestParam("title") String title,
                       @RequestParam(value = "description", required = false) String description,
                       @RequestParam("price") BigDecimal price,
                       @RequestParam("conditionLevel") String conditionLevel,
                       @RequestParam("contactInfo") String contactInfo,
                       @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                       @RequestParam(value = "clearImages", required = false, defaultValue = "false") boolean clearImages,
                       @RequestParam(value = "coverImageUrl", required = false) String coverImageUrl,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            String imageUrl = coverImageUrl;
            List<String> uploaded = uploadGoodsImagesIfPresent(imageFiles, userId);

            GoodsSaveRequest request = new GoodsSaveRequest();
            request.setCategoryId(categoryId);
            request.setTitle(title);
            request.setDescription(description);
            request.setPrice(price);
            request.setConditionLevel(conditionLevel);
            request.setContactInfo(contactInfo);
            if (!uploaded.isEmpty()) {
                imageUrl = uploaded.get(0);
                request.setImageUrls(uploaded);
                request.setReplaceImages(true);
            } else if (clearImages) {
                imageUrl = null;
                request.setImageUrls(new ArrayList<>());
                request.setReplaceImages(true);
            } else {
                request.setReplaceImages(false);
            }
            request.setCoverImageUrl(imageUrl);

            goodsService.updateGoods(userId, id, request);
            redirectAttributes.addFlashAttribute("successMessage", "商品已重新提交审核，审核通过后自动上架");
            return "redirect:/goods/" + id;
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/goods/" + id + "/edit";
        }
    }

    @PostMapping("/goods/{id}/off-shelf")
    public String offShelf(@PathVariable("id") Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            goodsService.offShelfGoods(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "商品已下架");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/goods/my";
    }

    private List<String> uploadGoodsImagesIfPresent(List<MultipartFile> files, Long userId) {
        List<String> urls = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return urls;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            FileUploadResponse response = fileStorageService.uploadGoodsImage(file, userId);
            urls.add(response.getUrl());
        }
        return urls;
    }

    private Long getLoginUserId(HttpSession session) {
        Object value = session.getAttribute(WebSessionKeys.LOGIN_USER_ID);
        if (value instanceof Long userId) {
            return userId;
        }
        if (value instanceof Integer userId) {
            return userId.longValue();
        }
        return null;
    }

    private Long getAdminId(HttpSession session) {
        Object value = session.getAttribute(WebSessionKeys.ADMIN_USER_ID);
        if (value instanceof Long adminId) {
            return adminId;
        }
        if (value instanceof Integer adminId) {
            return adminId.longValue();
        }
        return null;
    }
}

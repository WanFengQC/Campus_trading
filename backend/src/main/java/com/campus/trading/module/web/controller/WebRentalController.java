package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.category.service.CategoryService;
import com.campus.trading.module.file.dto.FileUploadResponse;
import com.campus.trading.module.file.service.FileStorageService;
import com.campus.trading.module.rental.dto.RentalItemSaveRequest;
import com.campus.trading.module.rental.service.RentalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class WebRentalController {

    private final RentalService rentalService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    public WebRentalController(RentalService rentalService,
                               CategoryService categoryService,
                               FileStorageService fileStorageService) {
        this.rentalService = rentalService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/rentals")
    public String rentals(@RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(value = "categoryId", required = false) Long categoryId,
                          @RequestParam(value = "minDailyRent", required = false) BigDecimal minDailyRent,
                          @RequestParam(value = "maxDailyRent", required = false) BigDecimal maxDailyRent,
                          Model model) {
        model.addAttribute("categories", categoryService.listActiveCategories());
        model.addAttribute("items", rentalService.searchAvailable(keyword, categoryId, minDailyRent, maxDailyRent));
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minDailyRent", minDailyRent);
        model.addAttribute("maxDailyRent", maxDailyRent);
        return "pages/rental-list";
    }

    @GetMapping("/rentals/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("item", rentalService.getDetail(id));
        return "pages/rental-detail";
    }

    @GetMapping("/rentals/publish")
    public String publishPage(Model model, HttpSession session) {
        if (getLoginUserId(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("categories", categoryService.listActiveCategories());
        return "pages/rental-publish";
    }

    @PostMapping("/rentals/publish")
    public String publish(@RequestParam("categoryId") Long categoryId,
                          @RequestParam("title") String title,
                          @RequestParam(value = "description", required = false) String description,
                          @RequestParam("dailyRent") BigDecimal dailyRent,
                          @RequestParam(value = "deposit", required = false) BigDecimal deposit,
                          @RequestParam("contactInfo") String contactInfo,
                          @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            RentalItemSaveRequest request = new RentalItemSaveRequest();
            request.setCategoryId(categoryId);
            request.setTitle(title);
            request.setDescription(description);
            request.setDailyRent(dailyRent);
            request.setDeposit(deposit);
            request.setContactInfo(contactInfo);
            request.setCoverImageUrl(uploadRentalImageIfPresent(imageFile, userId));
            Long itemId = rentalService.createItem(userId, request).getId();
            redirectAttributes.addFlashAttribute("successMessage", "租赁商品发布成功");
            return "redirect:/rentals/" + itemId;
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/rentals/publish";
        }
    }

    @GetMapping("/rentals/my")
    public String myRentals(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("items", rentalService.listOwnerItems(userId));
        return "pages/rental-my";
    }

    @PostMapping("/rentals/{id}/off-shelf")
    public String offShelf(@PathVariable("id") Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            rentalService.offShelf(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "租赁商品已下架");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rentals/my";
    }

    @PostMapping("/rentals/{id}/order")
    public String createOrder(@PathVariable("id") Long id,
                              @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam(value = "renterRemark", required = false) String renterRemark,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            rentalService.createOrder(userId, id, startDate, endDate, renterRemark);
            redirectAttributes.addFlashAttribute("successMessage", "租赁下单成功");
            return "redirect:/rental-orders";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/rentals/" + id;
        }
    }

    @GetMapping("/rental-orders")
    public String rentalOrders(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("orders", rentalService.listUserOrders(userId));
        return "pages/rental-orders";
    }

    @PostMapping("/rental-orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            rentalService.cancelOrder(userId, orderId);
            redirectAttributes.addFlashAttribute("successMessage", "订单已取消");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rental-orders";
    }

    @PostMapping("/rental-orders/{orderId}/owner-confirm")
    public String ownerConfirm(@PathVariable("orderId") Long orderId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            rentalService.ownerConfirm(userId, orderId);
            redirectAttributes.addFlashAttribute("successMessage", "已确认租赁订单");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rental-orders";
    }

    @PostMapping("/rental-orders/{orderId}/complete")
    public String complete(@PathVariable("orderId") Long orderId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            rentalService.renterComplete(userId, orderId);
            redirectAttributes.addFlashAttribute("successMessage", "订单已完成");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rental-orders";
    }

    @PostMapping("/rental-orders/{orderId}/renew")
    public String renew(@PathVariable("orderId") Long orderId,
                        @RequestParam("newEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            rentalService.renewOrder(userId, orderId, newEndDate);
            redirectAttributes.addFlashAttribute("successMessage", "续租成功");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rental-orders";
    }

    private String uploadRentalImageIfPresent(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        FileUploadResponse response = fileStorageService.uploadGoodsImage(file, userId);
        return response.getUrl();
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
}

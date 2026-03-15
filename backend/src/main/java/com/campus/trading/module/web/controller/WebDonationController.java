package com.campus.trading.module.web.controller;

import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.category.service.CategoryService;
import com.campus.trading.module.donation.dto.DonationItemSaveRequest;
import com.campus.trading.module.donation.service.DonationService;
import com.campus.trading.module.file.dto.FileUploadResponse;
import com.campus.trading.module.file.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebDonationController {

    private final DonationService donationService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    public WebDonationController(DonationService donationService,
                                 CategoryService categoryService,
                                 FileStorageService fileStorageService) {
        this.donationService = donationService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/donations")
    public String donations(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "categoryId", required = false) Long categoryId,
                            Model model) {
        model.addAttribute("categories", categoryService.listActiveCategories());
        model.addAttribute("items", donationService.searchAvailable(keyword, categoryId));
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        return "pages/donation-list";
    }

    @GetMapping("/donations/{id}")
    public String detail(@PathVariable("id") Long id, Model model, HttpSession session) {
        model.addAttribute("item", donationService.getDetail(id));
        model.addAttribute("loggedIn", getLoginUserId(session) != null);
        return "pages/donation-detail";
    }

    @GetMapping("/donations/publish")
    public String publishPage(Model model, HttpSession session) {
        if (getLoginUserId(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("categories", categoryService.listActiveCategories());
        model.addAttribute("loggedIn", true);
        return "pages/donation-publish";
    }

    @PostMapping("/donations/publish")
    public String publish(@RequestParam("categoryId") Long categoryId,
                          @RequestParam("title") String title,
                          @RequestParam(value = "description", required = false) String description,
                          @RequestParam("contactInfo") String contactInfo,
                          @RequestParam(value = "pickupAddress", required = false) String pickupAddress,
                          @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            DonationItemSaveRequest request = new DonationItemSaveRequest();
            request.setCategoryId(categoryId);
            request.setTitle(title);
            request.setDescription(description);
            request.setContactInfo(contactInfo);
            request.setPickupAddress(pickupAddress);
            request.setCoverImageUrl(uploadDonationImageIfPresent(imageFile, userId));
            Long itemId = donationService.createItem(userId, request).getId();
            redirectAttributes.addFlashAttribute("successMessage", "捐赠物品发布成功");
            return "redirect:/donations/" + itemId;
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/donations/publish";
        }
    }

    @GetMapping("/donations/my")
    public String myDonations(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("items", donationService.listDonorItems(userId));
        model.addAttribute("donorRecords", donationService.listDonorRecords(userId));
        model.addAttribute("loggedIn", true);
        return "pages/donation-my";
    }

    @PostMapping("/donations/{id}/off-shelf")
    public String offShelf(@PathVariable("id") Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            donationService.offShelf(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "捐赠物品已下架");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/donations/my";
    }

    @PostMapping("/donations/{id}/claim")
    public String claim(@PathVariable("id") Long id,
                        @RequestParam(value = "claimRemark", required = false) String claimRemark,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            donationService.claim(userId, id, claimRemark);
            redirectAttributes.addFlashAttribute("successMessage", "认领申请已提交，请等待捐赠方处理");
            return "redirect:/donation-records";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/donations/" + id;
        }
    }

    @GetMapping("/donation-records")
    public String records(Model model, HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("records", donationService.listClaimerRecords(userId));
        model.addAttribute("loggedIn", true);
        return "pages/donation-records";
    }

    @PostMapping("/donation-records/{recordId}/cancel")
    public String cancelClaim(@PathVariable("recordId") Long recordId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            donationService.cancelClaim(userId, recordId);
            redirectAttributes.addFlashAttribute("successMessage", "认领申请已取消");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/donation-records";
    }

    @PostMapping("/donation-records/{recordId}/approve")
    public String approveClaim(@PathVariable("recordId") Long recordId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            donationService.approveClaim(userId, recordId);
            redirectAttributes.addFlashAttribute("successMessage", "已同意认领申请");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/donations/my";
    }

    @PostMapping("/donation-records/{recordId}/reject")
    public String rejectClaim(@PathVariable("recordId") Long recordId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            donationService.rejectClaim(userId, recordId);
            redirectAttributes.addFlashAttribute("successMessage", "已拒绝认领申请");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/donations/my";
    }

    @PostMapping("/donation-records/{recordId}/complete")
    public String completeClaim(@PathVariable("recordId") Long recordId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            donationService.completeClaim(userId, recordId);
            redirectAttributes.addFlashAttribute("successMessage", "认领流程已完成");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/donation-records";
    }

    private String uploadDonationImageIfPresent(MultipartFile file, Long userId) {
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

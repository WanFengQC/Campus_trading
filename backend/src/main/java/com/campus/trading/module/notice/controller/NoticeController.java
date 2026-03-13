package com.campus.trading.module.notice.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.common.web.WebSessionKeys;
import com.campus.trading.module.notice.dto.NoticeResponse;
import com.campus.trading.module.notice.service.NoticeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ApiResponse<List<NoticeResponse>> listPublished(@RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(noticeService.listPublished(limit));
    }

    @GetMapping("/admin")
    public ApiResponse<List<NoticeResponse>> listAdmin(@RequestParam(value = "status", required = false) Integer status,
                                                       HttpSession session) {
        ensureAdminLogin(session);
        return ApiResponse.success(noticeService.listAdmin(status));
    }

    @PostMapping
    public ApiResponse<NoticeResponse> create(@RequestParam("title") String title,
                                              @RequestParam("content") String content,
                                              @RequestParam(value = "sortNo", required = false) Integer sortNo,
                                              HttpSession session) {
        String adminName = ensureAdminLogin(session);
        return ApiResponse.success(noticeService.createNotice(title, content, sortNo, adminName));
    }

    @PostMapping("/{noticeId}/update")
    public ApiResponse<NoticeResponse> update(@PathVariable("noticeId") Long noticeId,
                                              @RequestParam("title") String title,
                                              @RequestParam("content") String content,
                                              @RequestParam("status") Integer status,
                                              @RequestParam("sortNo") Integer sortNo,
                                              HttpSession session) {
        ensureAdminLogin(session);
        return ApiResponse.success(noticeService.updateNotice(noticeId, title, content, status, sortNo));
    }

    private String ensureAdminLogin(HttpSession session) {
        Object adminId = session.getAttribute(WebSessionKeys.ADMIN_USER_ID);
        if (!(adminId instanceof Long) && !(adminId instanceof Integer)) {
            throw new BusinessException("未登录管理员账号");
        }
        Object adminName = session.getAttribute(WebSessionKeys.ADMIN_USERNAME);
        if (adminName == null) {
            return "管理员";
        }
        return adminName.toString();
    }
}

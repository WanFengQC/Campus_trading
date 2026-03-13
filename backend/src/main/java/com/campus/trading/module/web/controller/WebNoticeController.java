package com.campus.trading.module.web.controller;

import com.campus.trading.module.notice.service.NoticeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebNoticeController {

    private final NoticeService noticeService;

    public WebNoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/notices")
    public String notices(Model model) {
        model.addAttribute("items", noticeService.listPublished(20));
        return "pages/notice-list";
    }
}

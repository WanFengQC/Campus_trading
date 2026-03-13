package com.campus.trading.module.notice.service;

import com.campus.trading.module.notice.dto.NoticeResponse;

import java.util.List;

public interface NoticeService {

    List<NoticeResponse> listPublished(Integer limit);

    List<NoticeResponse> listAdmin(Integer status);

    NoticeResponse createNotice(String title, String content, Integer sortNo, String publisher);

    NoticeResponse updateNotice(Long noticeId, String title, String content, Integer status, Integer sortNo);
}

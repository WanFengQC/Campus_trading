package com.campus.trading.module.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.common.exception.BusinessException;
import com.campus.trading.module.notice.dto.NoticeResponse;
import com.campus.trading.module.notice.entity.NoticeEntity;
import com.campus.trading.module.notice.mapper.NoticeMapper;
import com.campus.trading.module.notice.service.NoticeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeServiceImpl(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    @Override
    public List<NoticeResponse> listPublished(Integer limit) {
        int safeLimit = (limit == null || limit <= 0) ? 5 : Math.min(limit, 20);
        List<NoticeEntity> notices = noticeMapper.selectList(new LambdaQueryWrapper<NoticeEntity>()
            .eq(NoticeEntity::getStatus, 1)
            .orderByDesc(NoticeEntity::getSortNo)
            .orderByDesc(NoticeEntity::getCreatedAt)
            .last("limit " + safeLimit));
        return toResponses(notices);
    }

    @Override
    public List<NoticeResponse> listAdmin(Integer status) {
        LambdaQueryWrapper<NoticeEntity> wrapper = new LambdaQueryWrapper<NoticeEntity>()
            .orderByDesc(NoticeEntity::getSortNo)
            .orderByDesc(NoticeEntity::getCreatedAt);
        if (status != null) {
            wrapper.eq(NoticeEntity::getStatus, status);
        }
        return toResponses(noticeMapper.selectList(wrapper));
    }

    @Override
    public NoticeResponse createNotice(String title, String content, Integer sortNo, String publisher) {
        if (!StringUtils.hasText(title)) {
            throw new BusinessException("公告标题不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("公告内容不能为空");
        }
        NoticeEntity notice = new NoticeEntity();
        notice.setTitle(limit(title.trim(), 128));
        notice.setContent(limit(content.trim(), 2000));
        notice.setStatus(1);
        notice.setSortNo(sortNo == null ? 0 : sortNo);
        notice.setPublisher(StringUtils.hasText(publisher) ? limit(publisher.trim(), 64) : "管理员");
        noticeMapper.insert(notice);
        return toResponses(List.of(notice)).get(0);
    }

    @Override
    public NoticeResponse updateNotice(Long noticeId, String title, String content, Integer status, Integer sortNo) {
        NoticeEntity notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        if (StringUtils.hasText(title)) {
            notice.setTitle(limit(title.trim(), 128));
        }
        if (StringUtils.hasText(content)) {
            notice.setContent(limit(content.trim(), 2000));
        }
        if (sortNo != null) {
            notice.setSortNo(sortNo);
        }
        if (status != null) {
            if (status != 0 && status != 1) {
                throw new BusinessException("公告状态仅支持 0 或 1");
            }
            notice.setStatus(status);
        }
        noticeMapper.updateById(notice);
        return toResponses(List.of(notice)).get(0);
    }

    private List<NoticeResponse> toResponses(List<NoticeEntity> notices) {
        List<NoticeResponse> result = new ArrayList<>();
        if (notices == null || notices.isEmpty()) {
            return result;
        }
        for (NoticeEntity notice : notices) {
            result.add(NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .status(notice.getStatus())
                .sortNo(notice.getSortNo())
                .publisher(notice.getPublisher())
                .createdAt(notice.getCreatedAt())
                .build());
        }
        return result;
    }

    private String limit(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}

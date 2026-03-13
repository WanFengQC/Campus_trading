package com.campus.trading.module.notice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NoticeResponse {

    private Long id;

    private String title;

    private String content;

    private Integer status;

    private Integer sortNo;

    private String publisher;

    private LocalDateTime createdAt;
}

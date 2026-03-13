package com.campus.trading.module.notice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice")
public class NoticeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Integer status;

    private Integer sortNo;

    private String publisher;

    private LocalDateTime createdAt;
}

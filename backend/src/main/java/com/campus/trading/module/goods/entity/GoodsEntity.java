package com.campus.trading.module.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods")
public class GoodsEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;

    private Long categoryId;

    private String title;

    private String description;

    private BigDecimal price;

    private String conditionLevel;

    private String contactInfo;

    private String coverImageUrl;

    private String status;

    private String auditStatus;

    private String auditNote;

    private LocalDateTime auditTime;

    private LocalDateTime createdAt;
}

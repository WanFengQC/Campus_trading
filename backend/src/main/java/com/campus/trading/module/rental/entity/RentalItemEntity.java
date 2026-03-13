package com.campus.trading.module.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rental_item")
public class RentalItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerId;

    private Long categoryId;

    private String title;

    private String description;

    private BigDecimal dailyRent;

    private BigDecimal deposit;

    private String contactInfo;

    private String coverImageUrl;

    private String status;

    private LocalDateTime createdAt;
}

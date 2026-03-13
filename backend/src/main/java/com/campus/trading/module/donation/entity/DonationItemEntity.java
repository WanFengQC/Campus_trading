package com.campus.trading.module.donation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("donation_item")
public class DonationItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long donorId;

    private Long categoryId;

    private String title;

    private String description;

    private String contactInfo;

    private String pickupAddress;

    private String coverImageUrl;

    private String status;

    private LocalDateTime createdAt;
}

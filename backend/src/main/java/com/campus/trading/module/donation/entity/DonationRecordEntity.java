package com.campus.trading.module.donation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("donation_record")
public class DonationRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long donationItemId;

    private Long claimerId;

    private Long donorId;

    private String claimRemark;

    private String status;

    private LocalDateTime createdAt;
}

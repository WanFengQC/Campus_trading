package com.campus.trading.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_log")
public class OrderLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long operatorUserId;

    private String action;

    private String fromStatus;

    private String toStatus;

    private String note;

    private LocalDateTime createdAt;
}

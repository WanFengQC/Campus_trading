package com.campus.trading.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class TradeOrderEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long goodsId;

    private Long buyerId;

    private Long sellerId;

    private BigDecimal amount;

    private String buyerRemark;

    private LocalDateTime meetupTime;

    private String meetupLocation;

    private String meetupNote;

    private String status;

    private LocalDateTime createdAt;
}

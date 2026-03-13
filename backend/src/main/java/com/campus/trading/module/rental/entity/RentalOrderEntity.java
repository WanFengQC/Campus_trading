package com.campus.trading.module.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("rental_order")
public class RentalOrderEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long rentalItemId;

    private Long renterId;

    private Long ownerId;

    private BigDecimal dailyRent;

    private BigDecimal deposit;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal totalAmount;

    private String renterRemark;

    private String status;

    private LocalDateTime createdAt;
}

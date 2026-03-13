package com.campus.trading.module.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goods_image")
public class GoodsImageEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsId;

    private String imageUrl;

    private Integer sortNo;

    private LocalDateTime createdAt;
}

package com.campus.trading.module.category.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("category")
public class CategoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer sortNo;

    private Integer status;
}
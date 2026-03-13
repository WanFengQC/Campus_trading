package com.campus.trading.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminCategoryManageItemResponse {

    private Long categoryId;

    private String name;

    private Integer sortNo;

    private Integer status;
}

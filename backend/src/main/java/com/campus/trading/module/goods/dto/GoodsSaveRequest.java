package com.campus.trading.module.goods.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsSaveRequest {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题长度不能超过 128")
    private String title;

    @Size(max = 2000, message = "描述长度不能超过 2000")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于等于 0.01")
    private BigDecimal price;

    @NotBlank(message = "成色不能为空")
    @Size(max = 32, message = "成色长度不能超过 32")
    private String conditionLevel;

    @NotBlank(message = "联系方式不能为空")
    @Size(max = 64, message = "联系方式长度不能超过 64")
    private String contactInfo;

    @Size(max = 255, message = "图片地址长度不能超过 255")
    private String coverImageUrl;

    private List<String> imageUrls;

    private boolean replaceImages;
}

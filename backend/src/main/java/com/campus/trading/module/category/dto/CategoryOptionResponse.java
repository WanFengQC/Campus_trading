package com.campus.trading.module.category.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryOptionResponse {

    private Long id;
    private String name;
}
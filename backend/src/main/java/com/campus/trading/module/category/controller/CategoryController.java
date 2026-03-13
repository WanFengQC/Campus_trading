package com.campus.trading.module.category.controller;

import com.campus.trading.common.api.ApiResponse;
import com.campus.trading.module.category.dto.CategoryOptionResponse;
import com.campus.trading.module.category.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryOptionResponse>> list() {
        return ApiResponse.success(categoryService.listActiveCategories());
    }
}
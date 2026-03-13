package com.campus.trading.module.category.service;

import com.campus.trading.module.category.dto.CategoryOptionResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryOptionResponse> listActiveCategories();
}
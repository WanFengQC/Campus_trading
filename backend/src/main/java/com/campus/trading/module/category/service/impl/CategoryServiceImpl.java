package com.campus.trading.module.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.module.category.dto.CategoryOptionResponse;
import com.campus.trading.module.category.entity.CategoryEntity;
import com.campus.trading.module.category.mapper.CategoryMapper;
import com.campus.trading.module.category.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryOptionResponse> listActiveCategories() {
        ensureDefaults();
        List<CategoryEntity> entities = categoryMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
            .eq(CategoryEntity::getStatus, 1));

        entities.sort(Comparator.comparing(CategoryEntity::getSortNo).thenComparing(CategoryEntity::getId));
        List<CategoryOptionResponse> results = new ArrayList<>();
        for (CategoryEntity entity : entities) {
            results.add(CategoryOptionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build());
        }
        return results;
    }

    private void ensureDefaults() {
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<CategoryEntity>().eq(CategoryEntity::getStatus, 1));
        if (count != null && count > 0) {
            return;
        }

        String[] defaults = {"数码", "教材", "生活用品", "运动户外", "其他"};
        for (int i = 0; i < defaults.length; i++) {
            CategoryEntity category = new CategoryEntity();
            category.setName(defaults[i]);
            category.setSortNo(i + 1);
            category.setStatus(1);
            categoryMapper.insert(category);
        }
    }
}
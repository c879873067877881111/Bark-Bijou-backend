package com.smallnine.apiserver.service;

import com.smallnine.apiserver.dto.CategoryRequest;
import com.smallnine.apiserver.dto.CategoryResponse;
import com.smallnine.apiserver.entity.Category;

import java.util.List;

public interface CategoryService {

    Category findById(Long id);

    List<Category> findAll(int page, int size);

    List<Category> findActiveCategories(int page, int size);

    List<CategoryResponse> findTopCategories();

    List<CategoryResponse> getCategoryTree();

    List<CategoryResponse> findByParentId(Long parentId);

    Category createCategory(CategoryRequest request);

    Category updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);

    void updateCategoryStatus(Long id, Boolean isActive);

    long countCategories();

    long countActiveCategories();
}

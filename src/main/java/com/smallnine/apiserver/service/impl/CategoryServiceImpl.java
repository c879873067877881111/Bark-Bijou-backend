package com.smallnine.apiserver.service.impl;

import com.smallnine.apiserver.constants.enums.ResponseCode;
import com.smallnine.apiserver.dao.CategoryDao;
import com.smallnine.apiserver.dao.ProductDao;
import com.smallnine.apiserver.dto.CategoryRequest;
import com.smallnine.apiserver.dto.CategoryResponse;
import com.smallnine.apiserver.entity.Category;
import com.smallnine.apiserver.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl {
    
    private final CategoryDao categoryDao;
    private final ProductDao productDao;
    
    /**
     * 根據ID查詢分類
     */
    @org.springframework.cache.annotation.Cacheable(value = "categories", key = "#id")
    public Category findById(Long id) {
        return categoryDao.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.CATEGORY_NOT_FOUND));
    }
    
    /**
     * 查詢所有分類（分頁）
     */
    public List<Category> findAll(int page, int size) {
        int offset = page * size;
        return categoryDao.findAll(offset, size);
    }
    
    /**
     * 查詢啟用的分類（分頁）
     */
    public List<Category> findActiveCategories(int page, int size) {
        int offset = page * size;
        return categoryDao.findActiveCategories(offset, size);
    }
    
    /**
     * 查詢頂級分類
     */
    public List<CategoryResponse> findTopCategories() {
        List<Category> topCategories = categoryDao.findTopCategories();
        return topCategories.stream()
                .map(category -> {
                    CategoryResponse response = CategoryResponse.fromEntity(category);
                    // 設置商品數量
                    response.setProductCount(productDao.countByCategoryId(category.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 查詢分類樹（包含子分類）
     */
    public List<CategoryResponse> getCategoryTree() {
        List<Category> topCategories = categoryDao.findTopCategories();
        return topCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }
    
    /**
     * 根據父分類ID查詢子分類
     */
    public List<CategoryResponse> findByParentId(Long parentId) {
        List<Category> categories = categoryDao.findByParentId(parentId);
        return categories.stream()
                .map(category -> {
                    CategoryResponse response = CategoryResponse.fromEntity(category);
                    response.setProductCount(productDao.countByCategoryId(category.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 創建分類
     */
    @Transactional
    public Category createCategory(CategoryRequest request) {
        log.info("創建新分類: {}", request.getName());
        
        // 檢查分類名稱是否已存在
        if (categoryDao.existsByName(request.getName())) {
            throw new BusinessException(400, "分類名稱已存在: " + request.getName());
        }
        
        // 如果指定了父分類，檢查父分類是否存在
        if (request.getParentId() != null) {
            Category parentCategory = findById(request.getParentId());
            if (!parentCategory.getIsActive()) {
                throw new BusinessException(400, "父分類未啟用，無法添加子分類");
            }
        }
        
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        category.setImageUrl(request.getImageUrl());
        category.setIsActive(request.getIsActive());
        category.setCreatedAt(LocalDateTime.now());
        
        categoryDao.insert(category);
        log.info("分類創建成功: {}, ID: {}", category.getName(), category.getId());
        
        return category;
    }
    
    /**
     * 更新分類
     */
    @Transactional
    public Category updateCategory(Long id, CategoryRequest request) {
        log.info("更新分類: ID={}, Name={}", id, request.getName());
        
        Category existingCategory = findById(id);
        
        // 檢查分類名稱是否被其他分類使用
        if (!request.getName().equals(existingCategory.getName()) && 
            categoryDao.existsByName(request.getName())) {
            throw new BusinessException(400, "分類名稱已存在: " + request.getName());
        }
        
        // 如果指定了父分類，檢查父分類是否存在且不是自己
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException(400, "分類不能設置自己為父分類");
            }
            Category parentCategory = findById(request.getParentId());
            if (!parentCategory.getIsActive()) {
                throw new BusinessException(400, "父分類未啟用，無法設置為父分類");
            }
        }
        
        existingCategory.setName(request.getName());
        existingCategory.setDescription(request.getDescription());
        existingCategory.setParentId(request.getParentId());
        existingCategory.setImageUrl(request.getImageUrl());
        existingCategory.setIsActive(request.getIsActive());
        
        categoryDao.update(existingCategory);
        log.info("分類更新成功: {}", existingCategory.getName());
        
        return existingCategory;
    }
    
    /**
     * 刪除分類
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("刪除分類: ID={}", id);
        
        Category category = findById(id);
        
        // 檢查是否有子分類
        if (categoryDao.hasChildren(id)) {
            throw new BusinessException(400, "該分類下有子分類，無法刪除");
        }
        
        // 檢查是否有商品
        long productCount = productDao.countByCategoryId(id);
        if (productCount > 0) {
            throw new BusinessException(400, "該分類下有商品，無法刪除");
        }
        
        categoryDao.deleteById(id);
        log.info("分類刪除成功: {}", category.getName());
    }
    
    /**
     * 更新分類狀態
     */
    @Transactional
    public void updateCategoryStatus(Long id, Boolean isActive) {
        log.info("更新分類狀態: ID={}, isActive={}", id, isActive);
        
        Category category = findById(id);
        
        // 如果要停用分類，檢查是否有子分類
        if (!isActive && categoryDao.hasChildren(id)) {
            throw new BusinessException(400, "該分類下有子分類，無法停用");
        }
        
        int updatedRows = categoryDao.updateStatus(id, isActive);
        if (updatedRows == 0) {
            throw new BusinessException(ResponseCode.INTERNAL_SERVER_ERROR, "分類狀態更新失敗");
        }
        
        log.info("分類狀態更新成功: {} -> {}", category.getName(), isActive);
    }
    
    /**
     * 統計分類數量
     */
    public long countCategories() {
        return categoryDao.count();
    }
    
    /**
     * 統計啟用的分類數量
     */
    public long countActiveCategories() {
        return categoryDao.countActive();
    }
    
    /**
     * 構建分類樹
     */
    private CategoryResponse buildCategoryTree(Category category) {
        CategoryResponse response = CategoryResponse.fromEntity(category);
        response.setProductCount(productDao.countByCategoryId(category.getId()));
        
        // 查詢子分類
        List<Category> children = categoryDao.findByParentId(category.getId());
        if (!children.isEmpty()) {
            List<CategoryResponse> childResponses = children.stream()
                    .map(this::buildCategoryTree)
                    .collect(Collectors.toList());
            response.setChildren(childResponses);
        }
        
        return response;
    }
}
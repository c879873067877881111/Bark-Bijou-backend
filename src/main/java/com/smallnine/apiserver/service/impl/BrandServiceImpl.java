package com.smallnine.apiserver.service.impl;

import com.smallnine.apiserver.constants.enums.ResponseCode;
import com.smallnine.apiserver.dao.BrandDao;
import com.smallnine.apiserver.dto.BrandRequest;
import com.smallnine.apiserver.dto.BrandResponse;
import com.smallnine.apiserver.entity.Brand;
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
public class BrandServiceImpl {
    
    private final BrandDao brandDao;
    
    /**
     * 根據ID查詢品牌
     */
    public Brand findById(Long id) {
        return brandDao.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.BRAND_NOT_FOUND));
    }
    
    /**
     * 查詢所有品牌（分頁）
     */
    public List<Brand> findAll(int page, int size) {
        int offset = page * size;
        return brandDao.findAll(offset, size);
    }
    
    /**
     * 查詢所有品牌並返回回應格式（分頁）
     */
    public List<BrandResponse> findAllBrands(int page, int size) {
        List<Brand> brands = findAll(page, size);
        return brands.stream()
                .map(brand -> {
                    BrandResponse response = BrandResponse.fromEntity(brand);
                    // 設置該品牌下的商品數量
                    response.setProductCount(brandDao.countProductsByBrandId(brand.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 根據名稱搜索品牌
     */
    public List<BrandResponse> searchByName(String name, int page, int size) {
        int offset = page * size;
        List<Brand> brands = brandDao.searchByName(name, offset, size);
        return brands.stream()
                .map(brand -> {
                    BrandResponse response = BrandResponse.fromEntity(brand);
                    response.setProductCount(brandDao.countProductsByBrandId(brand.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 創建品牌
     */
    @Transactional
    public Brand createBrand(BrandRequest request) {
        log.info("創建新品牌: {}", request.getName());
        
        // 檢查品牌名稱是否已存在
        if (brandDao.existsByName(request.getName())) {
            throw new BusinessException(400, "品牌名稱已存在: " + request.getName());
        }
        
        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setCreatedAt(LocalDateTime.now());
        
        brandDao.insert(brand);
        log.info("品牌創建成功: {}, ID: {}", brand.getName(), brand.getId());
        
        return brand;
    }
    
    /**
     * 更新品牌
     */
    @Transactional
    public Brand updateBrand(Long id, BrandRequest request) {
        log.info("更新品牌: ID={}, Name={}", id, request.getName());
        
        Brand existingBrand = findById(id);
        
        // 檢查品牌名稱是否被其他品牌使用
        if (!request.getName().equals(existingBrand.getName()) && 
            brandDao.existsByName(request.getName())) {
            throw new BusinessException(400, "品牌名稱已存在: " + request.getName());
        }
        
        existingBrand.setName(request.getName());
        existingBrand.setDescription(request.getDescription());
        existingBrand.setLogoUrl(request.getLogoUrl());
        
        brandDao.update(existingBrand);
        log.info("品牌更新成功: {}", existingBrand.getName());
        
        return existingBrand;
    }
    
    /**
     * 刪除品牌
     */
    @Transactional
    public void deleteBrand(Long id) {
        log.info("刪除品牌: ID={}", id);
        
        Brand brand = findById(id);
        
        // 檢查是否有商品使用該品牌
        long productCount = brandDao.countProductsByBrandId(id);
        if (productCount > 0) {
            throw new BusinessException(400, "該品牌下有商品，無法刪除");
        }
        
        brandDao.deleteById(id);
        log.info("品牌刪除成功: {}", brand.getName());
    }
    
    /**
     * 統計品牌數量
     */
    public long countBrands() {
        return brandDao.count();
    }
    
    /**
     * 統計品牌下的商品數量
     */
    public long countProductsByBrand(Long brandId) {
        return brandDao.countProductsByBrandId(brandId);
    }
    
    /**
     * 獲取品牌回應對象
     */
    public BrandResponse getBrandResponse(Long id) {
        Brand brand = findById(id);
        BrandResponse response = BrandResponse.fromEntity(brand);
        response.setProductCount(brandDao.countProductsByBrandId(id));
        return response;
    }
}
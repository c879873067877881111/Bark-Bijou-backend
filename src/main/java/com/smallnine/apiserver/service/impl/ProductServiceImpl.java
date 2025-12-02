package com.smallnine.apiserver.service.impl;

import com.smallnine.apiserver.constants.enums.ResponseCode;
import com.smallnine.apiserver.dao.ProductDao;
import com.smallnine.apiserver.dto.ProductDTO;
import com.smallnine.apiserver.dto.ProductRequest;
import com.smallnine.apiserver.entity.Product;
import com.smallnine.apiserver.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl {
    
    private final ProductDao productDao;
    
    /**
     * 根據ID查詢商品
     */
    public Product findById(Long id) {
        return productDao.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.PRODUCT_NOT_FOUND));
    }
    
    /**
     * 查詢啟用的商品列表（分頁）
     */
    public List<Product> findActiveProducts(int page, int size) {
        int offset = page * size;
        return productDao.findActiveProducts(offset, size);
    }
    
    /**
     * 查詢所有商品列表（分頁）
     */
    public List<Product> findAll(int page, int size) {
        int offset = page * size;
        return productDao.findAll(offset, size);
    }
    
    /**
     * 根據分類查詢商品
     */
    public List<Product> findByCategoryId(Long categoryId) {
        return productDao.findByCategoryId(categoryId);
    }
    
    /**
     * 根據分類查詢商品（分頁）
     */
    public List<Product> findByCategory(Long categoryId, int page, int size) {
        int offset = page * size;
        return productDao.findByCategoryIdWithPaging(categoryId, offset, size);
    }
    
    /**
     * 根據品牌查詢商品
     */
    public List<Product> findByBrandId(Long brandId) {
        return productDao.findByBrandId(brandId);
    }
    
    /**
     * 搜索商品
     */
    public List<Product> searchByName(String name, int page, int size) {
        int offset = page * size;
        return productDao.searchByName(name, offset, size);
    }
    
    /**
     * 根據價格範圍查詢商品
     */
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, 
                                         int page, int size) {
        int offset = page * size;
        return productDao.findByPriceRange(minPrice, maxPrice, offset, size);
    }
    
    /**
     * 搜索商品（組合條件）
     */
    public List<Product> searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        int offset = page * size;
        return productDao.searchProducts(keyword, minPrice, maxPrice, offset, size);
    }
    
    /**
     * 創建商品（使用ProductRequest）
     */
    @Transactional
    public Product createProduct(ProductRequest request) {
        ProductDTO productDTO = convertRequestToDTO(request);
        return createProduct(productDTO);
    }
    
    /**
     * 創建商品
     */
    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        log.info("創建新商品: {}", productDTO.getName());
        
        // 檢查SKU是否已存在
        if (productDTO.getSku() != null && productDao.existsBySku(productDTO.getSku())) {
            throw new BusinessException(400, "SKU已存在: " + productDTO.getSku());
        }
        
        Product product = convertToEntity(productDTO);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        productDao.insert(product);
        log.info("商品創建成功: {}, ID: {}", product.getName(), product.getId());
        
        return product;
    }
    
    /**
     * 更新商品（使用ProductRequest）
     */
    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        ProductDTO productDTO = convertRequestToDTO(request);
        return updateProduct(id, productDTO);
    }
    
    /**
     * 更新商品
     */
    @Transactional
    public Product updateProduct(Long id, ProductDTO productDTO) {
        log.info("更新商品: ID={}, Name={}", id, productDTO.getName());
        
        Product existingProduct = findById(id);
        
        // 檢查SKU是否被其他商品使用
        if (productDTO.getSku() != null && !productDTO.getSku().equals(existingProduct.getSku())) {
            if (productDao.existsBySku(productDTO.getSku())) {
                throw new BusinessException(400, "SKU已存在: " + productDTO.getSku());
            }
        }
        
        updateEntityFromDTO(existingProduct, productDTO);
        existingProduct.setUpdatedAt(LocalDateTime.now());
        
        productDao.update(existingProduct);
        log.info("商品更新成功: {}", existingProduct.getName());
        
        return existingProduct;
    }
    
    /**
     * 刪除商品
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("刪除商品: ID={}", id);
        
        Product product = findById(id);
        productDao.deleteById(id);
        
        log.info("商品刪除成功: {}", product.getName());
    }
    
    /**
     * 更新商品庫存（返回boolean）
     */
    @Transactional
    public boolean updateStock(Long id, Integer stockQuantity) {
        try {
            updateStockInternal(id, stockQuantity);
            return true;
        } catch (Exception e) {
            log.error("更新商品庫存失敗: ID={}, Stock={}", id, stockQuantity, e);
            return false;
        }
    }
    
    /**
     * 更新商品庫存
     */
    @Transactional
    public void updateStockInternal(Long id, Integer stockQuantity) {
        long startTime = System.currentTimeMillis();
        
        // 記錄業務操作開始
        log.info("【庫存更新】開始更新商品庫存 - ID: {}, 目標數量: {}", id, stockQuantity);
        
        try {
            // 業務規則驗證
            if (stockQuantity < 0) {
                log.warn("【庫存更新】參數驗證失敗 - ID: {}, 庫存數量不能為負: {}", id, stockQuantity);
                throw new BusinessException(400, "庫存數量不能為負");
            }
            
            // 查詢商品信息
            Product product = findById(id);
            Integer oldStock = product.getStockQuantity();
            log.debug("【庫存更新】查詢商品信息 - ID: {}, 商品名稱: {}, 原庫存: {}", 
                     id, product.getName(), oldStock);
            
            // 執行庫存更新
            log.debug("【庫存更新】執行資料庫更新 - ID: {}, 原庫存: {} -> 新庫存: {}", 
                     id, oldStock, stockQuantity);
            int updatedRows = productDao.updateStock(id, stockQuantity);
            
            // 驗證更新結果
            if (updatedRows == 0) {
                log.error("【庫存更新】資料庫更新失敗 - ID: {}, 影響行數: {}", id, updatedRows);
                throw new BusinessException(ResponseCode.INTERNAL_SERVER_ERROR, "庫存更新失敗");
            }
            
            // 記錄成功操作
            long duration = System.currentTimeMillis() - startTime;
            log.info("【庫存更新】更新成功 - ID: {}, 商品: {}, 庫存變更: {} -> {}, 耗時: {}ms", 
                    id, product.getName(), oldStock, stockQuantity, duration);
                    
            // 性能監控
            if (duration > 100) {
                log.warn("【性能監控】庫存更新耗時較長 - ID: {}, 耗時: {}ms", id, duration);
            }
            
        } catch (BusinessException e) {
            // 業務異常記錄
            log.warn("【庫存更新】業務異常 - ID: {}, 異常信息: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 系統異常記錄
            long duration = System.currentTimeMillis() - startTime;
            log.error("【庫存更新】系統異常 - ID: {}, 耗時: {}ms, 異常信息: {}", 
                     id, duration, e.getMessage(), e);
            throw new BusinessException(ResponseCode.INTERNAL_SERVER_ERROR, "庫存更新系統異常");
        }
    }
    
    /**
     * 檢查庫存
     */
    public boolean checkStock(Long id, int quantity) {
        Product product = findById(id);
        return product.getStockQuantity() >= quantity;
    }
    
    /**
     * 減少商品庫存
     */
    @Transactional
    public boolean decreaseStock(Long id, Integer quantity) {
        log.info("減少商品庫存: ID={}, Quantity={}", id, quantity);
        
        if (quantity <= 0) {
            throw new BusinessException(400, "減少數量必須大於0");
        }
        
        int updatedRows = productDao.decreaseStock(id, quantity);
        
        if (updatedRows == 0) {
            log.warn("庫存不足或商品不存在: ID={}, Quantity={}", id, quantity);
            return false;
        }
        
        log.info("庫存減少成功: ID={}, Quantity={}", id, quantity);
        return true;
    }
    
    /**
     * 統計商品總數
     */
    public long countProducts() {
        return productDao.count();
    }
    
    /**
     * 統計分類下的商品數量
     */
    public long countByCategoryId(Long categoryId) {
        return productDao.countByCategoryId(categoryId);
    }
    
    /**
     * 將DTO轉換為Entity
     */
    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setSku(dto.getSku());
        product.setStockQuantity(dto.getStockQuantity());
        product.setBrandId(dto.getBrandId());
        product.setCategoryId(dto.getCategoryId());
        product.setIsActive(dto.getIsActive());
        product.setWeight(dto.getWeight());
        product.setDimensions(dto.getDimensions());
        return product;
    }
    
    /**
     * 從DTO更新Entity
     */
    private void updateEntityFromDTO(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setSku(dto.getSku());
        product.setStockQuantity(dto.getStockQuantity());
        product.setBrandId(dto.getBrandId());
        product.setCategoryId(dto.getCategoryId());
        product.setIsActive(dto.getIsActive());
        product.setWeight(dto.getWeight());
        product.setDimensions(dto.getDimensions());
    }
    
    /**
     * 將ProductRequest轉換為ProductDTO
     */
    private ProductDTO convertRequestToDTO(ProductRequest request) {
        ProductDTO dto = new ProductDTO();
        dto.setName(request.getName());
        dto.setDescription(request.getDescription());
        dto.setPrice(request.getPrice());
        dto.setSalePrice(request.getSalePrice());
        dto.setSku(request.getSku());
        dto.setStockQuantity(request.getStockQuantity());
        dto.setBrandId(request.getBrandId());
        dto.setCategoryId(request.getCategoryId());
        dto.setIsActive(request.getIsActive());
        dto.setWeight(request.getWeight());
        dto.setDimensions(request.getDimensions());
        return dto;
    }
}
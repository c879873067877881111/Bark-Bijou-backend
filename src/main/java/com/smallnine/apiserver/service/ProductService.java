package com.smallnine.apiserver.service;

import com.smallnine.apiserver.dto.ProductDTO;
import com.smallnine.apiserver.dto.ProductRequest;
import com.smallnine.apiserver.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    Product findById(Long id);

    List<Product> findActiveProducts(int page, int size);

    List<Product> findAll(int page, int size);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByCategory(Long categoryId, int page, int size);

    List<Product> findByBrandId(Long brandId);

    List<Product> searchByName(String name, int page, int size);

    List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size);

    List<Product> searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice, int page, int size);

    Product createProduct(ProductRequest request);

    Product createProduct(ProductDTO productDTO);

    Product updateProduct(Long id, ProductRequest request);

    Product updateProduct(Long id, ProductDTO productDTO);

    void deleteProduct(Long id);

    boolean updateStock(Long id, Integer stockQuantity);

    void updateStockInternal(Long id, Integer stockQuantity);

    boolean checkStock(Long id, int quantity);

    boolean decreaseStock(Long id, Integer quantity);

    long countProducts();

    long countByCategoryId(Long categoryId);
}

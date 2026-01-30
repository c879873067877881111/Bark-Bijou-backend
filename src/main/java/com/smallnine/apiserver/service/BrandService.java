package com.smallnine.apiserver.service;

import com.smallnine.apiserver.dto.BrandRequest;
import com.smallnine.apiserver.dto.BrandResponse;
import com.smallnine.apiserver.entity.Brand;

import java.util.List;

public interface BrandService {

    Brand findById(Long id);

    List<Brand> findAll(int page, int size);

    List<BrandResponse> findAllBrands(int page, int size);

    List<BrandResponse> searchByName(String name, int page, int size);

    Brand createBrand(BrandRequest request);

    Brand updateBrand(Long id, BrandRequest request);

    void deleteBrand(Long id);

    long countBrands();

    long countProductsByBrand(Long brandId);

    BrandResponse getBrandResponse(Long id);
}

package com.smallnine.apiserver.controller;

import com.smallnine.apiserver.dto.ApiResponse;
import com.smallnine.apiserver.dto.CategoryRequest;
import com.smallnine.apiserver.dto.CategoryResponse;
import com.smallnine.apiserver.entity.Category;
import com.smallnine.apiserver.service.impl.CategoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "分類管理", description = "商品分類相關 API - 查詢、創建、更新、刪除分類")
@Slf4j
public class CategoryController {
    
    private final CategoryServiceImpl categoryService;
    
    @Operation(summary = "獲取所有分類", description = "分頁獲取分類列表")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取分類列表"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Category> categories = categoryService.findAll(page, size);
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            log.error("獲取分類列表失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取分類列表失敗"));
        }
    }
    
    @Operation(summary = "獲取啟用的分類", description = "分頁獲取啟用的分類列表")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取分類列表"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Category>>> getActiveCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Category> categories = categoryService.findActiveCategories(page, size);
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            log.error("獲取啟用分類列表失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取分類列表失敗"));
        }
    }
    
    @Operation(summary = "獲取頂級分類", description = "獲取所有頂級分類（不包含子分類）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取頂級分類"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getTopCategories() {
        try {
            List<CategoryResponse> categories = categoryService.findTopCategories();
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            log.error("獲取頂級分類失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取頂級分類失敗"));
        }
    }
    
    @Operation(summary = "獲取分類樹", description = "獲取完整的分類樹結構（包含所有子分類）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取分類樹"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
        try {
            List<CategoryResponse> categoryTree = categoryService.getCategoryTree();
            return ResponseEntity.ok(ApiResponse.success(categoryTree));
        } catch (Exception e) {
            log.error("獲取分類樹失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取分類樹失敗"));
        }
    }
    
    @Operation(summary = "根據ID獲取分類", description = "根據分類ID獲取單個分類詳情")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取分類"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "分類不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryService.findById(id);
            return ResponseEntity.ok(ApiResponse.success(category));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("獲取分類失敗: id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取分類失敗"));
        }
    }
    
    @Operation(summary = "根據父分類獲取子分類", description = "根據父分類ID獲取所有子分類")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取子分類"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping("/{parentId}/children")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesByParent(@PathVariable Long parentId) {
        try {
            List<CategoryResponse> categories = categoryService.findByParentId(parentId);
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            log.error("獲取子分類失敗: parentId={}", parentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取子分類失敗"));
        }
    }
    
    @Operation(summary = "創建分類", description = "創建新分類")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "分類創建成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "請求參數無效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("分類創建成功", category));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("創建分類失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("創建分類失敗"));
        }
    }
    
    @Operation(summary = "更新分類", description = "根據ID更新分類訊息")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "分類更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "請求參數無效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "分類不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(ApiResponse.success("分類更新成功", category));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("更新分類失敗: id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新分類失敗"));
        }
    }
    
    @Operation(summary = "刪除分類", description = "根據ID刪除分類")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "分類刪除成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "分類不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("分類刪除成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("刪除分類失敗: id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("刪除分類失敗"));
        }
    }
    
    @Operation(summary = "更新分類狀態", description = "啟用或停用分類")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "分類狀態更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "狀態無效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "分類不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateCategoryStatus(
            @PathVariable Long id, 
            @RequestParam Boolean isActive) {
        try {
            categoryService.updateCategoryStatus(id, isActive);
            return ResponseEntity.ok(ApiResponse.success("分類狀態更新成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("更新分類狀態失敗: id={}, isActive={}", id, isActive, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新分類狀態失敗"));
        }
    }
    
    @Operation(summary = "統計分類數量", description = "獲取分類總數和啟用分類數量")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "統計成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<CategoryCountResponse>> getCategoryCount() {
        try {
            long totalCount = categoryService.countCategories();
            long activeCount = categoryService.countActiveCategories();
            
            CategoryCountResponse countResponse = new CategoryCountResponse(totalCount, activeCount);
            return ResponseEntity.ok(ApiResponse.success(countResponse));
        } catch (Exception e) {
            log.error("統計分類數量失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("統計分類數量失敗"));
        }
    }
    
    // 內部類用於統計回應
    public static class CategoryCountResponse {
        private long totalCount;
        private long activeCount;
        
        public CategoryCountResponse(long totalCount, long activeCount) {
            this.totalCount = totalCount;
            this.activeCount = activeCount;
        }
        
        public long getTotalCount() { return totalCount; }
        public long getActiveCount() { return activeCount; }
    }
}
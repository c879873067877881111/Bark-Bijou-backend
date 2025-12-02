package com.smallnine.apiserver.controller;

import com.smallnine.apiserver.dto.ApiResponse;
import com.smallnine.apiserver.dto.CreateOrderRequest;
import com.smallnine.apiserver.entity.Order;
import com.smallnine.apiserver.entity.OrderItem;
import com.smallnine.apiserver.entity.User;
import com.smallnine.apiserver.service.impl.OrderServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "訂單管理", description = "訂單相關 API - 創建、查詢、更新、取消訂單")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    
    private final OrderServiceImpl orderService;
    
    @Operation(summary = "獲取用戶訂單列表", description = "分頁獲取當前用戶的訂單列表")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取訂單列表"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("未授權訪問"));
            }
            
            User user = (User) userDetails;
            List<Order> orders = orderService.findUserOrders(user.getId(), page, size);
            return ResponseEntity.ok(ApiResponse.success(orders));
        } catch (Exception e) {
            log.error("獲取用戶訂單列表失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取訂單列表失敗"));
        }
    }
    
    @Operation(summary = "根據ID獲取訂單", description = "根據訂單ID獲取訂單詳情")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取訂單"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "訂單不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("未授權訪問"));
            }
            
            Order order = orderService.findById(id);
            User user = (User) userDetails;
            
            // 檢查訂單是否屬於當前用戶
            if (!order.getMemberId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.fail("無權限訪問此訂單"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("獲取訂單失敗: id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取訂單失敗"));
        }
    }
    
    @Operation(summary = "根據訂單號獲取訂單", description = "根據訂單號獲取訂單詳情")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取訂單"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "訂單不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<Order>> getOrderByNumber(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNumber) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("未授權訪問"));
            }
            
            Order order = orderService.findByOrderNumber(orderNumber);
            User user = (User) userDetails;
            
            // 檢查訂單是否屬於當前用戶
            if (!order.getMemberId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.fail("無權限訪問此訂單"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("根據訂單號獲取訂單失敗: orderNumber={}", orderNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取訂單失敗"));
        }
    }
    
    @Operation(summary = "獲取訂單項目", description = "獲取指定訂單的所有訂單項目")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功獲取訂單項目"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "訂單不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<List<OrderItem>>> getOrderItems(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("未授權訪問"));
            }
            
            // 先驗證訂單存在且屬於當前用戶
            Order order = orderService.findById(orderId);
            User user = (User) userDetails;
            
            if (!order.getMemberId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.fail("無權限訪問此訂單"));
            }
            
            List<OrderItem> orderItems = orderService.findOrderItems(orderId);
            return ResponseEntity.ok(ApiResponse.success(orderItems));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("獲取訂單項目失敗: orderId={}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("獲取訂單項目失敗"));
        }
    }
    
    @Operation(summary = "從購物車創建訂單", description = "從當前用戶的購物車創建新訂單")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "訂單創建成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "購物車為空或庫存不足"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("未授權訪問"));
            }
            
            User user = (User) userDetails;
            Order order = orderService.createOrderFromCart(
                    user.getId(), 
                    request.getShippingAddress(), 
                    request.getNotes()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("訂單創建成功", order));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("創建訂單失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("創建訂單失敗"));
        }
    }
    
    @Operation(summary = "更新訂單狀態", description = "更新訂單狀態（管理員功能）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "訂單狀態更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "狀態無效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "訂單不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Long statusId) {
        try {
            orderService.updateOrderStatus(orderId, statusId);
            return ResponseEntity.ok(ApiResponse.success("訂單狀態更新成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("更新訂單狀態失敗: orderId={}, statusId={}", orderId, statusId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新訂單狀態失敗"));
        }
    }
    
    @Operation(summary = "取消訂單", description = "取消指定的訂單")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "訂單取消成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "訂單狀態不允許取消"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "無權限操作此訂單"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "訂單不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("未授權訪問"));
            }
            
            User user = (User) userDetails;
            orderService.cancelOrder(orderId, user.getId());
            return ResponseEntity.ok(ApiResponse.success("訂單取消成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(e.getMessage()));
            } else if (e.getMessage().contains("無權限")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("取消訂單失敗: orderId={}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("取消訂單失敗"));
        }
    }
    
    @Operation(summary = "刪除訂單", description = "刪除指定的訂單（管理員功能）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "訂單刪除成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "訂單不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success("訂單刪除成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("刪除訂單失敗: orderId={}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("刪除訂單失敗"));
        }
    }
    
    @Operation(summary = "統計用戶訂單數量", description = "獲取當前用戶的訂單總數")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "獲取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "內部服務器錯誤")
    })
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getUserOrderCount(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("未授權訪問"));
            }
            
            User user = (User) userDetails;
            long count = orderService.countUserOrders(user.getId());
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("統計用戶訂單數量失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("統計訂單數量失敗"));
        }
    }
}
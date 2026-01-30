package com.smallnine.apiserver.dto;

import com.smallnine.apiserver.entity.OrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class OrderItemResponse {

    private Long id;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;

    public OrderItemResponse(OrderItem item) {
        this.id = item.getId();
        this.orderId = item.getOrderId();
        this.productId = item.getProductId();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.totalPrice = item.getTotalPrice();
        this.createdAt = item.getCreatedAt();
    }

    public static OrderItemResponse fromEntity(OrderItem item) {
        return new OrderItemResponse(item);
    }
}

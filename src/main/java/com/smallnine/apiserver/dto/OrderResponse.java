package com.smallnine.apiserver.dto;

import com.smallnine.apiserver.entity.Order;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long statusId;
    private BigDecimal totalAmount;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private String shippingAddress;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.statusId = order.getStatusId();
        this.totalAmount = order.getTotalAmount();
        this.shippingAmount = order.getShippingAmount();
        this.taxAmount = order.getTaxAmount();
        this.discountAmount = order.getDiscountAmount();
        this.shippingAddress = order.getShippingAddress();
        this.notes = order.getNotes();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
    }

    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(order);
    }
}

package com.smallnine.apiserver.constants.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING(1L, "待處理"),
    CONFIRMED(2L, "已確認"),
    PROCESSING(3L, "處理中"),
    SHIPPED(4L, "已出貨"),
    DELIVERED(5L, "已送達"),
    CANCELLED(6L, "已取消"),
    REFUNDED(7L, "已退款");

    private final Long id;
    private final String description;

    OrderStatus(Long id, String description) {
        this.id = id;
        this.description = description;
    }

    public static OrderStatus fromId(Long id) {
        for (OrderStatus status : values()) {
            if (status.id.equals(id)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status id: " + id);
    }

    public boolean canCancel() {
        return this == PENDING || this == CONFIRMED;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING -> newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED -> newStatus == REFUNDED;
            case CANCELLED, REFUNDED -> false;
        };
    }
}

package com.example.neworderfood.models;

import com.example.neworderfood.room.entities.InvoiceItemEntity;
import com.example.neworderfood.room.entities.OrderItemEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderItem implements Serializable {
    private int id;
    private int orderId;
    private int dishId;
    private int quantity;
    private String notes;
    private int discount = 0;  // %

    public OrderItem(int id, int orderId, int dishId, int quantity, String notes) {
        this.id = id;
        this.orderId = orderId;
        this.dishId = dishId;
        this.quantity = quantity;
        this.notes = notes;
    }

    // Getters
    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getDishId() { return dishId; }
    public int getQuantity() { return quantity; }
    public String getNotes() { return notes; }
    public int getDiscount() { return discount; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setDishId(int dishId) { this.dishId = dishId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setDiscount(int discount) { this.discount = discount; }

    public OrderItem copy(int newOrderId) {
        return new OrderItem(this.id, newOrderId, this.dishId, this.quantity, this.notes);
    }

    public static List<OrderItem> fromEntities(List<OrderItemEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(entity -> {
                    OrderItem item = new OrderItem(entity.id, entity.orderId, entity.dishId, entity.quantity, entity.notes);
                    item.setDiscount(entity.discount);  // FIX: Set trong map (entity in scope)
                    return item;
                })
                .collect(Collectors.toList());
    }

    // GIỮ NGUYÊN: Map from InvoiceItemEntity (không có discount → set 0)
    public static List<OrderItem> fromInvoiceItems(List<InvoiceItemEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(entity -> {
                    OrderItem item = new OrderItem(entity.id, 0 /* invoiceId không dùng ở đây */, entity.dishId, entity.quantity, entity.notes);
                    item.setDiscount(0);  // Default cho Invoice
                    return item;
                })
                .collect(Collectors.toList());
    }

    // GIỮ NGUYÊN: Map to OrderItemEntity
    public OrderItemEntity toEntity() {
        return new OrderItemEntity(id, orderId, dishId, quantity, notes, discount);
    }

    // GIỮ NGUYÊN: Map to InvoiceItemEntity
    public InvoiceItemEntity toInvoiceItemEntity(int invoiceId) {
        return new InvoiceItemEntity(0 /* auto */, invoiceId, dishId, quantity, notes);
    }
}

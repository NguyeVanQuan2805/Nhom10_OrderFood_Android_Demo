package com.example.neworderfood.models;

import java.io.Serializable;

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
}

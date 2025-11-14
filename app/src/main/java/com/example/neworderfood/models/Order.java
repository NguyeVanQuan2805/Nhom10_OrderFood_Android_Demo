package com.example.neworderfood.models;

import com.example.neworderfood.room.entities.OrderEntity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Order implements Serializable {
    private int id;
    private int tableNumber;
    private int totalAmount;
    private String status = "Pending";
    private Date createdAt = new Date();

    public Order(int id, int tableNumber, int totalAmount) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.totalAmount = totalAmount;
    }

    // Getters
    public int getId() { return id; }
    public int getTableNumber() { return tableNumber; }
    public int getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public Date getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public static List<Order> fromEntities(List<OrderEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(entity -> {
                    Date date = new Date();  // Fallback; parse nếu createdAt là String
                    // Nếu Entity.createdAt là String: Date date = new SimpleDateFormat("...").parse(entity.createdAt);
                    return new Order(entity.id, entity.tableNumber, entity.totalAmount);
                })
                .peek(order -> { /* Set status/date nếu cần */ })
                .collect(Collectors.toList());
    }

    // THÊM: Map to Entity (single)
    public OrderEntity toEntity() {
        // Giả sử createdAt là String trong Entity
        String createdStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(createdAt);
        return new OrderEntity(id, tableNumber, totalAmount, status, createdStr);
    }
}

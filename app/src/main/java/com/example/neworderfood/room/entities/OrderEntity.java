package com.example.neworderfood.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class OrderEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int tableNumber;
    public int totalAmount;
    @NonNull
    public String status;  // Default: "đang phục vụ"
    public String createdAt;  // TEXT (ISO date)

    public OrderEntity(int id, int tableNumber, int totalAmount, @NonNull String status, String createdAt) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }
}
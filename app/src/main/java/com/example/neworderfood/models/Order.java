package com.example.neworderfood.models;

import java.io.Serializable;
import java.util.Date;

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
}

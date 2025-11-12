package com.example.neworderfood.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Invoice implements Serializable {
    private int id;  // Số hóa đơn tự tăng
    private Date date;  // Ngày giờ
    private int tableNumber;
    private int totalAmount;
    private List<OrderItem> items;  // Danh sách món
    private int changeAmount;  // Tiền thừa

    public Invoice(int id, Date date, int tableNumber, int totalAmount, List<OrderItem> items, int changeAmount) {
        this.id = id;
        this.date = date;
        this.tableNumber = tableNumber;
        this.totalAmount = totalAmount;
        this.items = items;
        this.changeAmount = changeAmount;
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public int getChangeAmount() { return changeAmount; }
    public void setChangeAmount(int changeAmount) { this.changeAmount = changeAmount; }
}
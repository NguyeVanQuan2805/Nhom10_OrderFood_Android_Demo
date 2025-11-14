package com.example.neworderfood.room.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_items",
        foreignKeys = {
                @ForeignKey(entity = OrderEntity.class, parentColumns = "id", childColumns = "orderId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = DishEntity.class, parentColumns = "id", childColumns = "dishId", onDelete = ForeignKey.SET_NULL)
        })
public class OrderItemEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int orderId;
    public int dishId;
    public int quantity;
    public String notes;
    public int discount;  // Default 0

    public OrderItemEntity(int id, int orderId, int dishId, int quantity, String notes, int discount) {
        this.id = id;
        this.orderId = orderId;
        this.dishId = dishId;
        this.quantity = quantity;
        this.notes = notes;
        this.discount = discount;
    }
    public int getId() {
        return id;
    }
}
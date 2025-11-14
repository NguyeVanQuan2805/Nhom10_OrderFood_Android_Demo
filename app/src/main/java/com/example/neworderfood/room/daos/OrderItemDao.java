package com.example.neworderfood.room.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.neworderfood.room.entities.OrderItemEntity;

import java.util.List;

@Dao
public interface OrderItemDao {
    @Insert
    long addOrderItem(OrderItemEntity item);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    List<OrderItemEntity> getOrderItemsByOrderId(int orderId);

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    int deleteOrderItemsByOrderId(int orderId);

    // SỬA: Thêm method delete single item by ID
    @Query("DELETE FROM order_items WHERE id = :id")
    int deleteOrderItem(int id);
}
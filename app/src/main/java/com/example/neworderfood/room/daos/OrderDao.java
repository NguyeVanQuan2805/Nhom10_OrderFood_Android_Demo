package com.example.neworderfood.room.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.neworderfood.room.entities.OrderEntity;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    long addOrder(OrderEntity order);

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    List<OrderEntity> getAllOrders();  // Blocking (có thể thêm LiveData nếu cần)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    int updateOrderStatus(int orderId, String status);

    @Query("UPDATE orders SET totalAmount = :newTotal WHERE id = :orderId")
    int updateOrderTotal(int orderId, int newTotal);

    @Query("DELETE FROM orders WHERE id = :orderId")
    int deleteOrderById(int orderId);

    // THÊM vào OrderDao
    @Query("SELECT * FROM orders WHERE tableNumber = :tableNumber AND status != 'Paid' ORDER BY id DESC LIMIT 1")
    List<OrderEntity> getUnpaidOrdersByTable(int tableNumber);  // Return list, take first (latest)
}
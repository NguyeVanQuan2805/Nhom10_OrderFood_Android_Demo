package com.example.neworderfood.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.Order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public OrderDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    private void openReadable() {
        db = dbHelper.getReadableDatabase();
    }

    private void openWritable() {
        db = dbHelper.getWritableDatabase();
    }

    private void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public long addOrder(Order order) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_ORDER_TABLE, order.getTableNumber());
            values.put(DatabaseHelper.COL_ORDER_TOTAL, order.getTotalAmount());
            values.put(DatabaseHelper.COL_ORDER_STATUS, order.getStatus());
            values.put(DatabaseHelper.COL_ORDER_CREATED, new Date().toString());
            return db.insert(DatabaseHelper.TABLE_ORDERS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        openReadable();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ORDERS, null, null, null, null, null,
                DatabaseHelper.COL_ORDER_CREATED + " DESC")) {
            while (cursor.moveToNext()) {
                Order order = new Order(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TABLE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL))
                );
                order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS)));
                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return orders;
    }

    public void updateOrderStatus(int orderId, String status) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_ORDER_STATUS, status);
            db.update(DatabaseHelper.TABLE_ORDERS, values,
                    DatabaseHelper.COL_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }
    // Thêm vào package com.example.neworderfood.dao; (giữ nguyên import)
    public void updateOrderTotal(int orderId, int newTotal) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_ORDER_TOTAL, newTotal);
            int updated = db.update(DatabaseHelper.TABLE_ORDERS, values,
                    DatabaseHelper.COL_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
            Log.d("OrderDAO", "Updated total for order " + orderId + " to " + newTotal + " (rows: " + updated + ")");
        } catch (Exception e) {
            Log.e("OrderDAO", "Error updating total for order " + orderId, e);
            e.printStackTrace();
        } finally {
            close();
        }
    }
    public void deleteOrderById(int orderId) {
        openWritable();
        try {
            if (orderId <= 0) {
                Log.w("OrderDAO", "Invalid orderId: " + orderId + " - Skipping delete");
                return;
            }
            int deleted = db.delete(DatabaseHelper.TABLE_ORDERS,
                    DatabaseHelper.COL_ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});
            Log.d("OrderDAO", "Deleted Order " + orderId + " (affected rows: " + deleted + ")");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OrderDAO", "Error deleting Order " + orderId, e);
        } finally {
            close();
        }
    }
}
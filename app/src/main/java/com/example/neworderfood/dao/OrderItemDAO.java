package com.example.neworderfood.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public OrderItemDAO(Context context) {
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

    public long addOrderItem(OrderItem item) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_ITEM_ORDER_ID, item.getOrderId());
            values.put(DatabaseHelper.COL_ITEM_DISH_ID, item.getDishId());
            values.put(DatabaseHelper.COL_ITEM_QUANTITY, item.getQuantity());
            values.put(DatabaseHelper.COL_ITEM_NOTES, item.getNotes());
            values.put(DatabaseHelper.COL_ITEM_DISCOUNT, item.getDiscount());
            return db.insert(DatabaseHelper.TABLE_ORDER_ITEMS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        openReadable();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ORDER_ITEMS, null,
                DatabaseHelper.COL_ITEM_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)}, null, null, null)) {
            while (cursor.moveToNext()) {
                OrderItem item = new OrderItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_ORDER_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_DISH_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_NOTES))
                );
                item.setDiscount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ITEM_DISCOUNT)));
                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return items;
    }
    // Thêm vào package com.example.neworderfood.dao; (giữ nguyên import)

    public void deleteOrderItemsByOrderId(int orderId) {
        openWritable();
        try {
            int deleted = db.delete(DatabaseHelper.TABLE_ORDER_ITEMS,
                    DatabaseHelper.COL_ITEM_ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});
            Log.d("OrderItemDAO", "Deleted " + deleted + " items for order " + orderId);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OrderItemDAO", "Error deleting items for order " + orderId);
        } finally {
            close();
        }
    }
}
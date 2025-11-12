package com.example.neworderfood.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.Invoice;
import com.example.neworderfood.models.OrderItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoiceDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public InvoiceDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    private void openWritable() {
        db = dbHelper.getWritableDatabase();
    }

    private void openReadable() {
        db = dbHelper.getReadableDatabase();
    }

    private void close() {
        if (db != null && db.isOpen()) db.close();
    }

    // Lưu hóa đơn
    public long addInvoice(Invoice invoice) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put("date", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(invoice.getDate()));
            values.put("table_number", invoice.getTableNumber());
            values.put("total_amount", invoice.getTotalAmount());
            values.put("change_amount", invoice.getChangeAmount());
            long invoiceId = db.insert("invoices", null, values);

            // Lưu items
            if (invoiceId > 0 && invoice.getItems() != null) {
                for (OrderItem item : invoice.getItems()) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put("invoice_id", invoiceId);
                    itemValues.put("dish_id", item.getDishId());
                    itemValues.put("quantity", item.getQuantity());
                    itemValues.put("notes", item.getNotes());
                    db.insert("invoice_items", null, itemValues);
                }
            }
            return invoiceId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    // Lấy tất cả hóa đơn (sắp xếp theo ngày desc)
    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        openReadable();
        try (Cursor cursor = db.query("invoices", null, null, null, null, null, "date DESC")) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                Date date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                int table = cursor.getInt(cursor.getColumnIndexOrThrow("table_number"));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow("total_amount"));
                int change = cursor.getInt(cursor.getColumnIndexOrThrow("change_amount"));
                List<OrderItem> items = getInvoiceItemsByInvoiceId(id);
                invoices.add(new Invoice(id, date, table, total, items, change));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return invoices;
    }

    // Lấy items của hóa đơn
    public List<OrderItem> getInvoiceItemsByInvoiceId(int invoiceId) {
        List<OrderItem> items = new ArrayList<>();
        openReadable();
        try (Cursor cursor = db.query("invoice_items", null, "invoice_id = ?", new String[]{String.valueOf(invoiceId)}, null, null, null)) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int dishId = cursor.getInt(cursor.getColumnIndexOrThrow("dish_id"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
                items.add(new OrderItem(id, invoiceId, dishId, quantity, notes));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return items;
    }
}
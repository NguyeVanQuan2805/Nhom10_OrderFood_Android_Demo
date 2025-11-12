package com.example.neworderfood.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.Table;

import java.util.ArrayList;
import java.util.List;

public class TableDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public TableDAO(Context context) {
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

    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.query("tables", null, null, null, null, null, "number");
            if (cursor.moveToFirst()) {
                do {
                    Table table = new Table(
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_ID)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_NUMBER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_STATUS))
                    );
                    tables.add(table);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            Log.e("TableDAO", "Error loading tables: " + e.getMessage());
            // Có thể tạo bảng mặc định nếu không tồn tại
        }
        db.close();
        return tables;
    }

    public List<Table> getTablesByStatus(String status) {
        List<Table> tables = new ArrayList<>();
        openReadable();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_TABLES, null,
                DatabaseHelper.COL_TABLE_STATUS + " = ?",
                new String[]{status}, null, null, DatabaseHelper.COL_TABLE_NUMBER)) {
            while (cursor.moveToNext()) {
                Table table = new Table(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_NUMBER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_STATUS))
                );
                tables.add(table);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return tables;
    }

    public long addTable(Table table) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_TABLE_NUMBER, table.getNumber());
            values.put(DatabaseHelper.COL_TABLE_STATUS, table.getStatus());
            return db.insert(DatabaseHelper.TABLE_TABLES, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    public int updateTable(Table table) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_TABLE_NUMBER, table.getNumber());
            values.put(DatabaseHelper.COL_TABLE_STATUS, table.getStatus());
            return db.update(DatabaseHelper.TABLE_TABLES, values,
                    DatabaseHelper.COL_TABLE_ID + " = ?", new String[]{String.valueOf(table.getId())});
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    public int deleteTable(int id) {
        openWritable();
        try {
            return db.delete(DatabaseHelper.TABLE_TABLES,
                    DatabaseHelper.COL_TABLE_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    public Table getTableById(int id) {
        openReadable();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_TABLES, null,
                DatabaseHelper.COL_TABLE_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new Table(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_NUMBER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_STATUS))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return null;
    }

    public void updateTableStatus(int tableNumber, String status) {
        // Tìm ID từ number
        Table table = getTableByNumber(tableNumber);  // Thêm method này nếu chưa
        if (table != null) {
            table.setStatus(status);
            updateTable(table);
        }
    }
    public Table getTableByNumber(int number) {
        openReadable();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_TABLES, null,
                DatabaseHelper.COL_TABLE_NUMBER + " = ?",
                new String[]{String.valueOf(number)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new Table(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_NUMBER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TABLE_STATUS))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return null;
    }
}
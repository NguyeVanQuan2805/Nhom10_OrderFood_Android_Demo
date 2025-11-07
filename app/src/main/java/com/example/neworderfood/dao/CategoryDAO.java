// New CategoryDAO.java
package com.example.neworderfood.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public CategoryDAO(Context context) {
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

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        openReadable();
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_CATEGORIES, null, null, null, null, null, DatabaseHelper.COL_CAT_NAME)) {
            while (cursor.moveToNext()) {
                Category category = new Category(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_NAME))
                );
                categories.add(category);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return categories;
    }

    public Category getCategoryById(int id) {
        openReadable();
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_CATEGORIES, null,
                DatabaseHelper.COL_CAT_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new Category(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_NAME))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return null;
    }

    public long addCategory(Category category) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_CAT_NAME, category.getName());
            return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    public int updateCategory(Category category) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_CAT_NAME, category.getName());
            return db.update(DatabaseHelper.TABLE_CATEGORIES, values,
                    DatabaseHelper.COL_CAT_ID + " = ?", new String[]{String.valueOf(category.getId())});
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    public int deleteCategory(int id) {
        openWritable();
        try {
            // Note: Deleting category may require handling foreign keys (cascade or check)
            return db.delete(DatabaseHelper.TABLE_CATEGORIES,
                    DatabaseHelper.COL_CAT_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }
}
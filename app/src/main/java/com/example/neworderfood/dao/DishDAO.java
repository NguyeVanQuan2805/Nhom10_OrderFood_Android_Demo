
package com.example.neworderfood.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.Dish;

import java.util.ArrayList;
import java.util.List;

public class DishDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DishDAO(Context context) {
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

    public List<Dish> getDishesByCategory(int categoryId) {  // Changed to int
        List<Dish> dishes = new ArrayList<>();
        openReadable();
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_DISHES, null,
                DatabaseHelper.COL_DISH_CATEGORY + " = ?",
                new String[]{String.valueOf(categoryId)}, null, null, DatabaseHelper.COL_DISH_NAME)) {
            while (cursor.moveToNext()) {
                Dish dish = new Dish(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_PRICE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_CATEGORY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_IMAGE))
                );
                dishes.add(dish);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return dishes;
    }

    public List<Dish> getAllDishes() {
        List<Dish> dishes = new ArrayList<>();
        openReadable();
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_DISHES, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                Dish dish = new Dish(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_PRICE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_CATEGORY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_IMAGE))
                );
                dishes.add(dish);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return dishes;
    }

    public Dish getDishById(int id) {
        openReadable();
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_DISHES, null,
                DatabaseHelper.COL_DISH_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new Dish(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_PRICE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_CATEGORY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DISH_IMAGE))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return null;
    }

    // New methods
    public long addDish(Dish dish) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_DISH_NAME, dish.getName());
            values.put(DatabaseHelper.COL_DISH_PRICE, dish.getPrice());
            values.put(DatabaseHelper.COL_DISH_CATEGORY, dish.getCategoryId());
            values.put(DatabaseHelper.COL_DISH_CATEGORY, dish.getImageResource());
            return db.insert(DatabaseHelper.TABLE_DISHES, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    public int updateDish(Dish dish) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_DISH_NAME, dish.getName());
            values.put(DatabaseHelper.COL_DISH_PRICE, dish.getPrice());
            values.put(DatabaseHelper.COL_DISH_CATEGORY, dish.getCategoryId());
            values.put(DatabaseHelper.COL_DISH_CATEGORY, dish.getImageResource());
            return db.update(DatabaseHelper.TABLE_DISHES, values,
                    DatabaseHelper.COL_DISH_ID + " = ?", new String[]{String.valueOf(dish.getId())});
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    public int deleteDish(int id) {
        openWritable();
        try {
            return db.delete(DatabaseHelper.TABLE_DISHES,
                    DatabaseHelper.COL_DISH_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }
}
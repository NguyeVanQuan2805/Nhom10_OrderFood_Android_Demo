package com.example.neworderfood.dao;

import static com.example.neworderfood.database.DatabaseHelper.COL_USER_ID;
import static com.example.neworderfood.database.DatabaseHelper.COL_USER_PASSWORD;
import static com.example.neworderfood.database.DatabaseHelper.COL_USER_ROLE;
import static com.example.neworderfood.database.DatabaseHelper.COL_USER_USERNAME;
import static com.example.neworderfood.database.DatabaseHelper.TABLE_USERS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public UserDAO(Context context) {
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

    // Authenticate user
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        openReadable();
        try (Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                User user = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE))
                );
                users.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return users;
    }
    public User getUserByUsernameAndPassword(String username, String password) {
        openReadable();
        try (Cursor cursor = db.query(
                TABLE_USERS, null,
                COL_USER_USERNAME + " = ? AND " + COL_USER_PASSWORD + " = ?",
                new String[]{username, password}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return null;
    }

    // Optional: Add user (for admin to add new employee)
    public long addUser(User user) {
        if (isUsernameExists(user.getUsername())) {
            return -1;  // Hoặc throw custom exception
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());  // Nên hash password thực tế!
        values.put("role", user.getRole());
        // Thêm các trường khác nếu có (email, etc.)
        long id = db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return id;
    }

    // Optional: Update user (change password, etc.)
    public int updateUser(User user) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_USER_USERNAME, user.getUsername());
            if (!user.getPassword().isEmpty()) {  // Chỉ update nếu không empty
                values.put(COL_USER_PASSWORD, user.getPassword());
            }
            values.put(COL_USER_ROLE, user.getRole());
            return db.update(TABLE_USERS, values,
                    COL_USER_ID + " = ?", new String[]{String.valueOf(user.getId())});
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    // Optional: Delete user
    public int deleteUser(int id) {
        openWritable();
        try {
            return db.delete(TABLE_USERS,
                    COL_USER_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id"}, "username = ?", new String[]{username}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
}
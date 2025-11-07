package com.example.neworderfood.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.User;

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
    public User getUserByUsernameAndPassword(String username, String password) {
        openReadable();
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COL_USER_USERNAME + " = ? AND " + DatabaseHelper.COL_USER_PASSWORD + " = ?",
                new String[]{username, password}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE))
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
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_USER_USERNAME, user.getUsername());
            values.put(DatabaseHelper.COL_USER_PASSWORD, user.getPassword());
            values.put(DatabaseHelper.COL_USER_ROLE, user.getRole());
            return db.insert(DatabaseHelper.TABLE_USERS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }

    // Optional: Update user (change password, etc.)
    public int updateUser(User user) {
        openWritable();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_USER_PASSWORD, user.getPassword());
            values.put(DatabaseHelper.COL_USER_ROLE, user.getRole());
            return db.update(DatabaseHelper.TABLE_USERS, values,
                    DatabaseHelper.COL_USER_ID + " = ?", new String[]{String.valueOf(user.getId())});
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
            return db.delete(DatabaseHelper.TABLE_USERS,
                    DatabaseHelper.COL_USER_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            close();
        }
    }
}
package com.example.neworderfood.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String username;  // UNIQUE
    @NonNull
    public String password;
    @NonNull
    public String role;  // "admin" or "employee"

    public UserEntity(int id, @NonNull String username, @NonNull String password, @NonNull String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
}
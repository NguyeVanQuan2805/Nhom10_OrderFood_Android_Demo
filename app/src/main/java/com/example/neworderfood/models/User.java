package com.example.neworderfood.models;

import com.example.neworderfood.room.entities.UserEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User implements Serializable {
    private int id;
    private String username;
    private String password;
    private String role;  // "admin" or "employee"

    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

    public static List<User> fromEntities(List<UserEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(entity -> new User(entity.id, entity.username, entity.password, entity.role))
                .collect(Collectors.toList());
    }
    // Thêm vào lớp User
    public static User fromEntity(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(entity.getId(), entity.getUsername(), entity.getPassword(), entity.getRole());
    }
    // THÊM: Map to Entity (single)
    public UserEntity toEntity() {
        return new UserEntity(id, username, password, role);
    }

}
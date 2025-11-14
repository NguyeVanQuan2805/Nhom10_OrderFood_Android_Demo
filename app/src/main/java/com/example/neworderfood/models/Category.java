package com.example.neworderfood.models;
// New Category.java model
import com.example.neworderfood.room.entities.CategoryEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Category implements Serializable {
    private int id;
    private String name;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }

    public static List<Category> fromEntities(List<CategoryEntity> entities) {
        if (entities == null) return new ArrayList<>();
        return entities.stream().map(e -> new Category(e.id, e.name)).collect(Collectors.toList());
    }

    public CategoryEntity toEntity() {
        return new CategoryEntity(id, name);
    }
}
package com.example.neworderfood.models;

import com.example.neworderfood.room.entities.DishEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Dish implements Serializable {
    private int id;
    private String name;
    private int price;
    private int categoryId;
    private int imageResource;  // Resource ID cho ảnh mặc định
    private String imageBase64; // Base64 string cho ảnh tải lên

    public Dish(int id, String name, int price, int categoryId, int imageResource) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.imageResource = imageResource;
        this.imageBase64 = null;
    }

    public Dish(int id, String name, int price, int categoryId, int imageResource, String imageBase64) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.imageResource = imageResource;
        this.imageBase64 = imageBase64;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getCategoryId() { return categoryId; }
    public int getImageResource() { return imageResource; }
    public String getImageBase64() { return imageBase64; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(int price) { this.price = price; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    // Phương thức tiện ích để lấy ảnh hiển thị
    public boolean hasCustomImage() {
        return imageBase64 != null && !imageBase64.isEmpty();
    }

    public static Dish fromEntity(DishEntity entity) {
        if (entity == null) return null;
        return new Dish(entity.id, entity.name, entity.price, entity.categoryId, entity.imageResource, entity.imageBase64);
    }

    public static List<Dish> fromEntities(List<DishEntity> entities) {
        if (entities == null) return new ArrayList<>();
        return entities.stream().map(e -> new Dish(e.id, e.name, e.price, e.categoryId, e.imageResource, e.imageBase64)).collect(Collectors.toList());
    }
    public DishEntity toEntity() {
        return new DishEntity(id, name, price, categoryId, imageResource, imageBase64);
    }
}
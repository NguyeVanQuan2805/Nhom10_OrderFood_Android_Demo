package com.example.neworderfood.models;

import java.io.Serializable;

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
}
package com.example.neworderfood.models;

import java.io.Serializable;

public class Dish implements Serializable {
    private int id;
    private String name;
    private int price;  // VND
    private int categoryId;
    private int imageResource;  // NEW: Drawable resource ID (e.g., R.drawable.bun_ca)

    public Dish(int id, String name, int price, int categoryId, int imageResource) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.imageResource = imageResource;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getCategoryId() { return categoryId; }
    public int getImageResource() { return imageResource; }  // NEW

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(int price) { this.price = price; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }  // NEW
}
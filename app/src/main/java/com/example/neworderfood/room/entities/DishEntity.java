package com.example.neworderfood.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "dishes",
        foreignKeys = @ForeignKey(entity = CategoryEntity.class,
                parentColumns = "id", childColumns = "categoryId", onDelete = ForeignKey.CASCADE))
public class DishEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String name;
    public int price;
    public int categoryId;
    public int imageResource;
    public String imageBase64;  // TEXT

    public DishEntity(int id, @NonNull String name, int price, int categoryId, int imageResource, String imageBase64) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.imageResource = imageResource;
        this.imageBase64 = imageBase64;
    }
}
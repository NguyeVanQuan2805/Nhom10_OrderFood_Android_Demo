package com.example.neworderfood.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String name;

    public CategoryEntity(int id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }
}
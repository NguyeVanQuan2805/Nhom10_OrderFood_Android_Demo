package com.example.neworderfood.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tables")
public class TableEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int number;
    @NonNull
    public String status;  // Default: "Còn trống"

    public TableEntity(int id, int number, @NonNull String status) {
        this.id = id;
        this.number = number;
        this.status = status;
    }
}
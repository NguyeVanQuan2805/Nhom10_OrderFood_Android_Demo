package com.example.neworderfood.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "invoices")
public class InvoiceEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String date;  // TEXT (formatted)
    public int tableNumber;
    public int totalAmount;
    public int changeAmount;

    public InvoiceEntity(int id, @NonNull String date, int tableNumber, int totalAmount, int changeAmount) {
        this.id = id;
        this.date = date;
        this.tableNumber = tableNumber;
        this.totalAmount = totalAmount;
        this.changeAmount = changeAmount;
    }
}
package com.example.neworderfood.room.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "invoice_items",
        foreignKeys = @ForeignKey(entity = InvoiceEntity.class,
                parentColumns = "id",
                childColumns = "invoiceId",
                onDelete = ForeignKey.CASCADE))
public class InvoiceItemEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int invoiceId;
    public int dishId;
    public int quantity;
    public String notes;

    public InvoiceItemEntity(int id, int invoiceId, int dishId, int quantity, String notes) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.dishId = dishId;
        this.quantity = quantity;
        this.notes = notes;
    }
    public int getId() { return id; }
    public int getInvoiceId() { return invoiceId; }
    public int getDishId() { return dishId; }
    public int getQuantity() { return quantity; }
    public String getNotes() { return notes; }

    public InvoiceItemEntity toInvoiceItemEntity(int invoiceId) {
        return new InvoiceItemEntity(0 /* auto */, invoiceId, dishId, quantity, notes);  // Đảm bảo invoiceId >0
    }
}
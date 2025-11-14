package com.example.neworderfood.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.neworderfood.room.entities.InvoiceItemEntity;

import java.util.List;

@Dao
public interface InvoiceItemDao {

    @Query("SELECT * FROM invoice_items")
    List<InvoiceItemEntity> getAllInvoiceItems();

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    List<InvoiceItemEntity> getItemsByInvoiceId(int invoiceId);

    // SỬA: Thêm insert single item (cho loop add)
    @Insert
    long addInvoiceItem(InvoiceItemEntity item);

    @Insert
    void insertInvoiceItems(List<InvoiceItemEntity> items);

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    void deleteItemsByInvoiceId(int invoiceId);

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY id ASC")
    List<InvoiceItemEntity> getInvoiceItemsByInvoiceId(int invoiceId);

    // Optional: LiveData version nếu cần observe
    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY id ASC")
    LiveData<List<InvoiceItemEntity>> getInvoiceItemsByInvoiceIdLive(int invoiceId);

}
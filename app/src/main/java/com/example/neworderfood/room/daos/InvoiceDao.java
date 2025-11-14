package com.example.neworderfood.room.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.neworderfood.room.entities.InvoiceEntity;
import com.example.neworderfood.room.entities.InvoiceItemEntity;

import java.util.List;

@Dao
public interface InvoiceDao {
    @Insert
    long addInvoice(InvoiceEntity invoice);

    @Query("SELECT * FROM invoices ORDER BY date DESC")
    List<InvoiceEntity> getAllInvoices();

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    List<InvoiceItemEntity> getInvoiceItemsByInvoiceId(int invoiceId);

    @Query("SELECT * FROM invoice_items")
    List<InvoiceItemEntity> getAllInvoiceItems();
    @Insert
    @Transaction
    default long insertInvoiceWithItems(InvoiceEntity invoice, List<InvoiceItemEntity> items) {
        return 0;
    }
}
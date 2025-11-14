package com.example.neworderfood.models;

import com.example.neworderfood.room.entities.InvoiceEntity;
import com.example.neworderfood.room.entities.InvoiceItemEntity;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Invoice implements Serializable {
    private int id;  // Số hóa đơn tự tăng
    private Date date;  // Ngày giờ
    private int tableNumber;
    private int totalAmount;
    private List<OrderItem> items;  // Danh sách món
    private int changeAmount;  // Tiền thừa

    public Invoice(int id, Date date, int tableNumber, int totalAmount, List<OrderItem> items, int changeAmount) {
        this.id = id;
        this.date = date;
        this.tableNumber = tableNumber;
        this.totalAmount = totalAmount;
        this.items = items;
        this.changeAmount = changeAmount;
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public int getChangeAmount() { return changeAmount; }
    public void setChangeAmount(int changeAmount) { this.changeAmount = changeAmount; }

    public static List<Invoice> fromEntities(List<InvoiceEntity> entities, List<InvoiceItemEntity> allItems) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(entity -> {
                    Date parsedDate = parseDate(entity.date);
                    // FIX: Sử dụng fromInvoiceItems thay vì fromEntities (type match)
                    List<OrderItem> mappedItems = OrderItem.fromInvoiceItems(getItemsByInvoiceId(allItems, entity.id));
                    return new Invoice(entity.id, parsedDate, entity.tableNumber, entity.totalAmount, mappedItems, entity.changeAmount);
                })
                .collect(Collectors.toList());
    }

    // GIỮ NGUYÊN: Helper method
    private static List<InvoiceItemEntity> getItemsByInvoiceId(List<InvoiceItemEntity> allItems, int invoiceId) {
        if (allItems == null || allItems.isEmpty()) {
            return new ArrayList<>();
        }
        return allItems.stream()
                .filter(item -> item.invoiceId == invoiceId)
                .collect(Collectors.toList());
    }

    // GIỮ NGUYÊN: Parse date helper
    private static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    // CẬP NHẬT: Map to Entity + items
    public InvoiceEntity toEntity() {
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
        return new InvoiceEntity(id, dateStr, tableNumber, totalAmount, changeAmount);
    }

    // GIỮ NGUYÊN: Method để lấy items as entities (sử dụng toInvoiceItemEntity)
    public List<InvoiceItemEntity> getItemsAsEntities() {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return items.stream()
                .map(item -> item.toInvoiceItemEntity(this.id))  // Dùng this.id (sau setId)
                .collect(Collectors.toList());
    }
    public static List<OrderItem> fromInvoiceItems(List<InvoiceItemEntity> entities) {
        if (entities == null || entities.isEmpty()) return new ArrayList<>();
        return entities.stream()
                .map(entity -> new OrderItem(entity.id, 0, entity.dishId, entity.quantity, entity.notes != null ? entity.notes : ""))
                .peek(item -> item.setDiscount(0))  // Default
                .collect(Collectors.toList());
    }
}
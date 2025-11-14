package com.example.neworderfood.models;

import com.example.neworderfood.room.entities.TableEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Table implements Serializable {
    private int id;
    private int number;
    private String status;  // "Available", "Occupied"

    public Table(int id, int number, String status) {
        this.id = id;
        this.number = number;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public int getNumber() { return number; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNumber(int number) { this.number = number; }
    public void setStatus(String status) { this.status = status; }

    public static List<Table> fromEntities(List<TableEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(entity -> new Table(entity.id, entity.number, entity.status))
                .collect(Collectors.toList());
    }

    // THÊM: Map to Entity (single)
    public TableEntity toEntity() {
        return new TableEntity(id, number, status);
    }
}
package com.example.neworderfood.models;

import java.io.Serializable;

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
}
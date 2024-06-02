package com.example.librecord;

public class ReservationRecord {
    private int id;
    private String name;
    private String details;
    private String status;

    public ReservationRecord(int id, String name, String details,String status) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }
    public String getStatus() {
        return status;
    }
}

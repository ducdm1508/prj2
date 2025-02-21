package com.cyber.server.model;

import java.time.LocalDateTime;

public class Computer {
    private int id;
    private String name;
    private Status status;
    private String specifications;
    private String ipAddress;
    private LocalDateTime lastMaintenanceDate;
    private Room room;

    public Computer() {;}

    public Computer(String name, Status status, String specifications, String ipAddress, LocalDateTime lastMaintenanceDate, Room room) {
        this.name = name;
        this.status = status;
        this.specifications = specifications;
        this.ipAddress = ipAddress;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.room = room;
    }

    // Getter and Setter Methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
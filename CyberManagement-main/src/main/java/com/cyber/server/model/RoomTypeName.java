package com.cyber.server.model;

public enum RoomTypeName {
    STANDARD("Standard Room"),
    VIP("Vip Room");

    private final String description;

    RoomTypeName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

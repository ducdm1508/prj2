package com.cyber.server.model;

public class RoomType {
    private int id;
    private RoomTypeName roomTypeName;
    private String description;

    public RoomType() {
    }

    public RoomType(int id, RoomTypeName roomTypeName, String description) {
        this.id = id;
        this.roomTypeName = roomTypeName;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RoomTypeName getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(RoomTypeName roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
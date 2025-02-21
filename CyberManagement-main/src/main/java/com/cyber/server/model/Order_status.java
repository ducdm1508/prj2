package com.cyber.server.model;

public enum Order_status {
    PENDING("Pending"),
    COMPLETED("Completed"),
    CANCELED("Canceled");

    private final String StatusLabel;

    Order_status(String statusLabel) {
        StatusLabel = statusLabel;
    }
}

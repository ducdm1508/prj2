package com.cyber.server.model;

import java.time.LocalDateTime;

public class Account {
    private int user_id;
    private String username;
    private String password;
    private double balance;
    private LocalDateTime createdDateColumn;


    public Account() {;}

    public Account(int user_id, String username, String password, double balance, LocalDateTime createdDateColumn) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.createdDateColumn = createdDateColumn;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedDateColumn() {
        return createdDateColumn;
    }

    public void setCreatedDateColumn(LocalDateTime createdDateColumn) {
        this.createdDateColumn = createdDateColumn;
    }
}
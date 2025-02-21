package com.cyber.server.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private int orderId;
    private Account account;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private Order_status status;

    public Order() {}

    public Order(int orderId, Account account, LocalDateTime orderDate, BigDecimal totalAmount, Order_status status) {
        this.orderId = orderId;
        this.account = account;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Order_status getStatus() {
        return status;
    }

    public void setStatus(Order_status status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

}

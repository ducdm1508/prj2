package com.cyber.server.model;

public class Order_Details {
    private int id;
    private Order order;
    private Food food;
    private String quantity;
    private double price;
    private double totalprice;
    private double discount;

    public Order_Details(int id, Order order, Food food, String quantity, double price, double totalprice, double discount) {
        this.id = id;
        this.order = order;
        this.food = food;
        this.quantity = quantity;
        this.price = price;
        this.totalprice = totalprice;
        this.discount = discount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(double totalprice) {
        this.totalprice = totalprice;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }
}

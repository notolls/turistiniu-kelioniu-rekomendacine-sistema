package com.example.projectkrs.model;

public class ShopBackground {

    private String name;
    private int price;
    private String drawable;

    public ShopBackground() {} // Reikalingas Firestore

    public ShopBackground(String name, int price, String drawable) {
        this.name = name;
        this.price = price;
        this.drawable = drawable;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getDrawable() { return drawable; }
}
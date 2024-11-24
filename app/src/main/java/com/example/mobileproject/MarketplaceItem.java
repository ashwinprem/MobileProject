package com.example.mobileproject;

public class MarketplaceItem {
    private String name;
    private String description;
    private double price;
    private String imageUrl;

    // Default constructor required for Firebase
    public MarketplaceItem() {}

    public MarketplaceItem(String name, String description, double price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

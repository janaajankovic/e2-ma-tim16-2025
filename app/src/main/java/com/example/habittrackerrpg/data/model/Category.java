package com.example.habittrackerrpg.data.model;

import com.google.firebase.firestore.Exclude;

public class Category {
    private String id;
    private String name;
    private String color;

    public Category() {}

    public Category(String name, String color) {
        this.name = name;
        this.color = color;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
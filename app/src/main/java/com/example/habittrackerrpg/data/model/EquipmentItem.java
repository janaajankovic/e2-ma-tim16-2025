package com.example.habittrackerrpg.data.model;

import com.google.firebase.firestore.Exclude;

public abstract class EquipmentItem {

    @Exclude
    private String id;
    private String name;
    private String description;
    private EquipmentType type;
    private int cost; // Cijena u novčićima

    public EquipmentItem() {}

    public EquipmentItem(String name, String description, EquipmentType type, int cost) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.cost = cost;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public EquipmentType getType() { return type; }
    public void setType(EquipmentType type) { this.type = type; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
}
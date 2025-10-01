package com.example.habittrackerrpg.data.model;

public class Clothing extends EquipmentItem {

    public enum ClothingType { GLOVES, SHIELD, BOOTS }

    private ClothingType clothingType;
    private int effectValue; // Vrijednost efekta u procentima
    private int durationInBattles;

    public Clothing() {
        super();
        this.setType(EquipmentType.CLOTHING);
        this.durationInBattles = 2;
    }

    public Clothing(String name, String description, int cost, ClothingType clothingType, int effectValue, String icon) {
        super(name, description, EquipmentType.CLOTHING, cost, icon);
        this.clothingType = clothingType;
        this.effectValue = effectValue;
        this.durationInBattles = 2;
    }

    public ClothingType getClothingType() { return clothingType; }
    public void setClothingType(ClothingType clothingType) { this.clothingType = clothingType; }
    public int getEffectValue() { return effectValue; }
    public void setEffectValue(int effectValue) { this.effectValue = effectValue; }
    public int getDurationInBattles() { return durationInBattles; }
    public void setDurationInBattles(int durationInBattles) { this.durationInBattles = durationInBattles; }
}
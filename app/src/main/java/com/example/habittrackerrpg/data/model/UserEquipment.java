package com.example.habittrackerrpg.data.model;

import com.google.firebase.firestore.Exclude;

public class UserEquipment {

    @Exclude
    private String id;
    private String userId;
    private String equipmentId;
    private EquipmentType type;

    private boolean isActive; // Da li je oprema trenutno aktivirana
    private int battlesRemaining; // Za odjeću, koliko borbi još traje (počinje sa 2)
    private double currentUpgradeBonus; // Za oružje, koliki je bonus od unapređenja

    public UserEquipment() {}

    public UserEquipment(String userId, String equipmentId, EquipmentType type) {
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.type = type;
        this.isActive = false;

        if (type == EquipmentType.CLOTHING) {
            this.battlesRemaining = 2;
        } else {
            this.battlesRemaining = 0;
        }

        this.currentUpgradeBonus = 0.0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    public EquipmentType getType() { return type; }
    public void setType(EquipmentType type) { this.type = type; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public int getBattlesRemaining() { return battlesRemaining; }
    public void setBattlesRemaining(int battlesRemaining) { this.battlesRemaining = battlesRemaining; }
    public double getCurrentUpgradeBonus() { return currentUpgradeBonus; }
    public void setCurrentUpgradeBonus(double currentUpgradeBonus) { this.currentUpgradeBonus = currentUpgradeBonus; }
}
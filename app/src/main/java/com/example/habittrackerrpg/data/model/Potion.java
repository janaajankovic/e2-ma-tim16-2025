package com.example.habittrackerrpg.data.model;

public class Potion extends EquipmentItem {

    private boolean isPermanent;
    private int ppBoostPercent;

    public Potion() {
        super();
        this.setType(EquipmentType.POTION);
    }

    public Potion(String name, String description, int cost, boolean isPermanent, int ppBoostPercent, String icon) {
        super(name, description, EquipmentType.POTION, cost, icon);
        this.isPermanent = isPermanent;
        this.ppBoostPercent = ppBoostPercent;
    }

    public boolean isPermanent() { return isPermanent; }
    public void setPermanent(boolean permanent) { isPermanent = permanent; }
    public int getPpBoostPercent() { return ppBoostPercent; }
    public void setPpBoostPercent(int ppBoostPercent) { this.ppBoostPercent = ppBoostPercent; }
}
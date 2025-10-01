package com.example.habittrackerrpg.data.model;

import com.google.firebase.firestore.Exclude;

public class Weapon extends EquipmentItem {

    public enum WeaponType { SWORD, BOW_AND_ARROW }

    private WeaponType weaponType;
    private double effectValue; // Početni efekat u procentima (npr. 5.0 za 5%)
    private double upgradeBonus; // Dodatni procenat od unapređenja

    public Weapon() {
        super();
        this.setType(EquipmentType.WEAPON);
    }

    public Weapon(String name, String description, WeaponType weaponType, double effectValue, String icon) {
        super(name, description, EquipmentType.WEAPON, 0, icon); // Oružje se ne kupuje
        this.weaponType = weaponType;
        this.effectValue = effectValue;
        this.upgradeBonus = 0.0;
    }

    // Ukupan efekat je zbir osnovne vrijednosti i bonusa od unapređenja
    @Exclude
    public double getTotalEffectValue() {
        return effectValue + upgradeBonus;
    }

    public WeaponType getWeaponType() { return weaponType; }
    public void setWeaponType(WeaponType weaponType) { this.weaponType = weaponType; }
    public double getEffectValue() { return effectValue; }
    public void setEffectValue(double effectValue) { this.effectValue = effectValue; }
    public double getUpgradeBonus() { return upgradeBonus; }
    public void setUpgradeBonus(double upgradeBonus) { this.upgradeBonus = upgradeBonus; }
}
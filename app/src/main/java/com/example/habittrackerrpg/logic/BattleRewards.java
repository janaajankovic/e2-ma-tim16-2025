package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.EquipmentItem;

public class BattleRewards {

    private final long coinsAwarded;
    private final EquipmentItem equipmentAwarded;

    public BattleRewards(long coinsAwarded, EquipmentItem equipmentAwarded) {
        this.coinsAwarded = coinsAwarded;
        this.equipmentAwarded = equipmentAwarded;
    }

    public long getCoinsAwarded() {
        return coinsAwarded;
    }

    public EquipmentItem getEquipmentAwarded() {
        return equipmentAwarded;
    }

    public boolean hasEquipment() {
        return equipmentAwarded != null;
    }
}
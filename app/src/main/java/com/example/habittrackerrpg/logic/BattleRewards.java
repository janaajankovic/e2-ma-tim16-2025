package com.example.habittrackerrpg.logic;

// import com.example.habittrackerrpg.data.model.Equipment; // Uključićemo kasnije

public class BattleRewards {

    private final long coinsAwarded;
    // private final Equipment equipmentAwarded; // Polje za opremu, za sada komentarisano

    public BattleRewards(long coinsAwarded /*, Equipment equipmentAwarded */) {
        this.coinsAwarded = coinsAwarded;
        // this.equipmentAwarded = equipmentAwarded;
    }

    public long getCoinsAwarded() {
        return coinsAwarded;
    }

    /*
    public Equipment getEquipmentAwarded() {
        return equipmentAwarded;
    }

    public boolean hasEquipment() {
        return equipmentAwarded != null;
    }
    */
}
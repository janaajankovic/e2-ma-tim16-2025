package com.example.habittrackerrpg.logic;

public class BattleTurnResult {

    public enum AttackResult {
        HIT,
        MISS
    }

    private final AttackResult result;
    private final long damageDealt;

    public BattleTurnResult(AttackResult result, long damageDealt) {
        this.result = result;
        this.damageDealt = damageDealt;
    }

    public AttackResult getResult() {
        return result;
    }

    public long getDamageDealt() {
        return damageDealt;
    }

    public boolean wasHit() {
        return result == AttackResult.HIT;
    }
}
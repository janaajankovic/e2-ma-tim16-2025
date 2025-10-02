package com.example.habittrackerrpg.logic;

import java.util.Random;

public class BossFightUseCase {

    private final Random randomGenerator;

    public BossFightUseCase() {
        this.randomGenerator = new Random();
    }

    public BattleTurnResult executeAttack(long userPp, int hitChancePercentage) {
        int randomNumber = randomGenerator.nextInt(100);

        if (randomNumber < hitChancePercentage) {
            return new BattleTurnResult(BattleTurnResult.AttackResult.HIT, userPp);
        } else {
            return new BattleTurnResult(BattleTurnResult.AttackResult.MISS, 0);
        }
    }
}
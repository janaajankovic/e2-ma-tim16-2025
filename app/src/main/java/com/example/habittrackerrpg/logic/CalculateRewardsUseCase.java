package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Boss;
import java.util.Random;

public class CalculateRewardsUseCase {

    private final Random randomGenerator;

    public CalculateRewardsUseCase() {
        this.randomGenerator = new Random();
    }


    public BattleRewards execute(Boss boss, long initialHp, long remainingHp) {
        long baseCoins = calculateBaseCoinsForBoss(boss.getLevel());
        long finalCoins = 0;

        boolean equipmentDropped = false;

        if (remainingHp <= 0) {
            finalCoins = baseCoins;
            if (randomGenerator.nextInt(100) < 20) {
                equipmentDropped = true;
            }
        }
        else if ((initialHp - remainingHp) >= (initialHp / 2.0)) {
            finalCoins = baseCoins / 2;
            // Šansa za opremu se takođe smanjuje upola (sa 20% na 10%)
            if (randomGenerator.nextInt(100) < 10) {
                equipmentDropped = true;
            }
        }
        // Slučaj 3: Naneto je manje od 50% štete
        else {
            finalCoins = 0; // Nema nagrade
            equipmentDropped = false;
        }

        // TODO: Kada se implementira oprema, ovde dodati logiku za generisanje
        // Equipment finalEquipment = null;
        // if (equipmentDropped) {
        //     // finalEquipment = generateRandomEquipment();
        // }

        return new BattleRewards(finalCoins /*, finalEquipment */);
    }

    private long calculateBaseCoinsForBoss(int bossLevel) {
        if (bossLevel <= 1) {
            return 200;
        }

        double coins = 200.0;
        for (int i = 2; i <= bossLevel; i++) {
            coins *= 1.20;
        }

        return Math.round(coins);
    }
}
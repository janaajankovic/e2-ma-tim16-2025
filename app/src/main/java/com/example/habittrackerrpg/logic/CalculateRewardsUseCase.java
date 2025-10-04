package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Boss;
import com.example.habittrackerrpg.data.model.EquipmentItem; // NOVI IMPORT
import com.example.habittrackerrpg.data.model.EquipmentType; // NOVI IMPORT

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CalculateRewardsUseCase {

    private final Random randomGenerator;

    public CalculateRewardsUseCase() {
        this.randomGenerator = new Random();
    }

    public BattleRewards execute(Boss boss, long initialHp, long remainingHp, List<EquipmentItem> allPossibleEquipment) {
        long baseCoins = calculateBaseCoinsForBoss(boss.getLevel());
        long finalCoins = 0;
        boolean equipmentDropped = false;

        if (remainingHp <= 0) {
            finalCoins = baseCoins;
            if (randomGenerator.nextInt(100) < 20) {
                equipmentDropped = true;
            }
        } else if ((initialHp - remainingHp) >= (initialHp / 2.0)) {
            finalCoins = baseCoins / 2;
            if (randomGenerator.nextInt(100) < 10) {
                equipmentDropped = true;
            }
        } else {
            finalCoins = 0;
            equipmentDropped = false;
        }

        EquipmentItem finalEquipment = null;
        if (equipmentDropped) {
            finalEquipment = generateRandomEquipment(allPossibleEquipment);
        }

        return new BattleRewards(finalCoins, finalEquipment);
    }

    private EquipmentItem generateRandomEquipment(List<EquipmentItem> allPossibleEquipment) {
        if (allPossibleEquipment == null || allPossibleEquipment.isEmpty()) {
            return null;
        }

        EquipmentType typeToDrop = (randomGenerator.nextInt(100) < 5) ? EquipmentType.WEAPON : EquipmentType.CLOTHING;

        List<EquipmentItem> droppableItems = allPossibleEquipment.stream()
                .filter(item -> item.getType() == typeToDrop)
                .collect(Collectors.toList());

        if (droppableItems.isEmpty()) {
            return null;
        }

        int randomIndex = randomGenerator.nextInt(droppableItems.size());
        return droppableItems.get(randomIndex);
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
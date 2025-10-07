package com.example.habittrackerrpg.logic;

import android.util.Log;
import com.example.habittrackerrpg.data.model.Boss;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.BossRepository;

import java.util.List;
import java.util.Optional;

public class GenerateBossUseCase {

    private static final String TAG = "BossLogic";


    public Boss execute(User user, List<Boss> allDefinedBosses, BossRepository bossRepository) {
        Log.d(TAG, "--- Checking for Boss ---");
        Log.d(TAG, "--- Checking for Boss ---");

        if (user == null) {
            Log.e(TAG, "User object is null!");
            return null;
        }
        if (allDefinedBosses == null) {
            Log.e(TAG, "List of all bosses is null!");
            return null;
        }

        Log.d(TAG, "User Level: " + user.getLevel());
        Log.d(TAG, "Highest Boss Defeated: " + user.getHighestBossDefeatedLevel());
        Log.d(TAG, "All Bosses Count: " + allDefinedBosses.size());

        if (user.getLastBossFightAttemptLevel() == user.getLevel()) {
            Log.w(TAG, "User has already attempted the boss fight at this level. No fight until next level up.");
            return null;
        }

        int targetBossLevel = user.getHighestBossDefeatedLevel() + 1;
        Log.d(TAG, "Target Boss Level: " + targetBossLevel);

        if (user.getLevel() <= targetBossLevel) {
            Log.w(TAG, "User is not high enough level. No fight.");
            return null;
        }

        Log.d(TAG, "User is eligible to fight. Searching for boss...");

        Optional<Boss> bossOptional = allDefinedBosses.stream()
                .filter(boss -> boss.getLevel() == targetBossLevel)
                .findFirst();

        if (bossOptional.isPresent()) {
            Log.d(TAG, "Boss found in DB: " + bossOptional.get().getName());
            return bossOptional.get();
        } else {
            Log.d(TAG, "Boss NOT FOUND for level " + targetBossLevel + ". Generating new boss...");

            Boss newBoss;
            if (targetBossLevel == 1) {
                newBoss = new Boss(1, 200, "Grunt, The First Guardian", "boss_level_1.json");
            } else {
                int previousBossLevel = targetBossLevel - 1;
                Optional<Boss> previousBossOptional = allDefinedBosses.stream()
                        .filter(b -> b.getLevel() == previousBossLevel)
                        .findFirst();

                long newHp = Math.round(200 * Math.pow(2.5, targetBossLevel - 1));
                Log.d(TAG, "Calculated HP for level " + targetBossLevel + ": " + newHp);


                String animationName;
                String bossName;
                if (targetBossLevel <= 4) {
                    animationName = "boss_level_" + targetBossLevel + ".json";
                } else {
                    animationName = "boss_level_4.json";
                }
                Log.d(TAG, "Selected animation for new boss: " + animationName);

                if (targetBossLevel == 1) {
                    bossName = "Grunt, The First Guardian";
                } else if (targetBossLevel == 2) {
                    bossName = "Korgath, The Stone Sentinel";
                } else if (targetBossLevel == 3) {
                    bossName = "Malakor, The Shadow Watcher";
                } else {
                    bossName = "Guardian of Level " + targetBossLevel;
                }

                newBoss = new Boss(targetBossLevel, newHp, bossName, animationName);
            }

            bossRepository.addNewBoss(newBoss);

            return newBoss;
        }
    }
}
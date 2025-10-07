package com.example.habittrackerrpg.logic;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.EquipmentType;
import com.example.habittrackerrpg.data.model.MissionStatus;
import com.example.habittrackerrpg.data.model.SpecialMission;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class EndMissionWorker extends Worker {

    private static final String TAG = "EndMissionWorker";
    private final FirebaseFirestore db;

    public EndMissionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        String missionId = getInputData().getString("MISSION_ID");
        if (missionId == null || missionId.isEmpty()) return Result.failure();

        Log.d(TAG, "Worker started for mission: " + missionId);
        DocumentReference missionRef = db.collection("specialMissions").document(missionId);

        try {
            SpecialMission mission = Tasks.await(missionRef.get()).toObject(SpecialMission.class);
            if (mission == null || mission.getStatus() != MissionStatus.ACTIVE) {
                Log.w(TAG, "Mission not found or not active. Worker finishing.");
                return Result.success();
            }

            applyOverdueTasksBonus(mission);

            SpecialMission finalMissionState = Tasks.await(missionRef.get()).toObject(SpecialMission.class);
            if (finalMissionState == null) return Result.failure();

            if (finalMissionState.getCurrentBossHp() <= 0) { // USPEH
                Log.d(TAG, "Mission " + missionId + " was successful!");
                WriteBatch rewardsBatch = db.batch();
                rewardsBatch.update(missionRef, "status", MissionStatus.SUCCESS);

                QuerySnapshot progressSnapshot = Tasks.await(missionRef.collection("progress").get());
                List<String> memberIds = new ArrayList<>();
                for (DocumentSnapshot doc : progressSnapshot.getDocuments()) {
                    memberIds.add(doc.getId());
                }

                List<EquipmentItem> allPotions = Tasks.await(db.collection("shop_equipment").whereEqualTo("type", "POTION").get()).toObjects(EquipmentItem.class);
                List<EquipmentItem> allClothing = Tasks.await(db.collection("shop_equipment").whereEqualTo("type", "CLOTHING").get()).toObjects(EquipmentItem.class);
                Random random = new Random();

                for (String memberId : memberIds) {
                    DocumentReference userRef = db.collection("users").document(memberId);
                    User user = Tasks.await(userRef.get()).toObject(User.class);
                    if (user == null) continue;

                    int nextBossLevel = user.getHighestBossDefeatedLevel() + 1;
                    long baseCoinReward = calculateBaseCoinsForBoss(nextBossLevel);
                    long finalCoinReward = baseCoinReward / 2;
                    rewardsBatch.update(userRef, "coins", FieldValue.increment(finalCoinReward));

                    if (!allPotions.isEmpty()) {
                        EquipmentItem potionReward = allPotions.get(random.nextInt(allPotions.size()));
                        UserEquipment userPotion = new UserEquipment(memberId, potionReward.getId(), EquipmentType.POTION);
                        rewardsBatch.set(userRef.collection("inventory").document(), userPotion);
                    }
                    if (!allClothing.isEmpty()) {
                        EquipmentItem clothingReward = allClothing.get(random.nextInt(allClothing.size()));
                        UserEquipment userClothing = new UserEquipment(memberId, clothingReward.getId(), EquipmentType.CLOTHING);
                        rewardsBatch.set(userRef.collection("inventory").document(), userClothing);
                    }
                    rewardsBatch.update(userRef, "successfulMissions", FieldValue.increment(1));
                }
                Tasks.await(rewardsBatch.commit());
                Log.d(TAG, "Rewards distributed for mission " + missionId);

            } else {
                Log.d(TAG, "Mission " + missionId + " failed. Final HP: " + finalMissionState.getCurrentBossHp());
                Tasks.await(missionRef.update("status", MissionStatus.FAIL));
            }

            return Result.success();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error executing mission end task", e);
            return Result.retry();
        }
    }

    private void applyOverdueTasksBonus(SpecialMission mission) throws ExecutionException, InterruptedException {
        Log.d(TAG, "Applying 'No Overdue Tasks' bonus check...");
        QuerySnapshot progressSnapshot = Tasks.await(db.collection("specialMissions").document(mission.getId()).collection("progress").get());

        WriteBatch bonusBatch = db.batch();
        int totalBonusDamage = 0;

        for (DocumentSnapshot progressDoc : progressSnapshot.getDocuments()) {
            String memberId = progressDoc.getId();

            Query overdueQuery = db.collectionGroup("instances")
                    .whereEqualTo("userId", memberId)
                    .whereEqualTo("status", "UNCOMPLETED")
                    .whereGreaterThanOrEqualTo("instanceDate", mission.getStartDate())
                    .whereLessThanOrEqualTo("instanceDate", mission.getEndDate())
                    .limit(1);

            QuerySnapshot overdueResult = Tasks.await(overdueQuery.get());

            if (overdueResult.isEmpty()) {
                Log.d(TAG, "User " + memberId + " had no overdue tasks. Applying 10 HP bonus damage.");
                totalBonusDamage += 10;

                DocumentReference progressRef = progressDoc.getReference();
                bonusBatch.update(progressRef, "totalDamageDealt", FieldValue.increment(10));
            } else {
                Log.d(TAG, "User " + memberId + " had overdue tasks. No bonus.");
            }
        }

        if (totalBonusDamage > 0) {
            Log.d(TAG, "Total bonus damage from 'No Overdue Tasks': " + totalBonusDamage);
            DocumentReference missionRef = db.collection("specialMissions").document(mission.getId());
            bonusBatch.update(missionRef, "currentBossHp", FieldValue.increment(-totalBonusDamage));
            Tasks.await(bonusBatch.commit());
        } else {
            Log.d(TAG, "No users qualified for the 'No Overdue Tasks' bonus.");
        }
    }

    private long calculateBaseCoinsForBoss(int bossLevel) {
        if (bossLevel <= 1) return 200;
        double coins = 200.0;
        for (int i = 2; i <= bossLevel; i++) {
            coins *= 1.20;
        }
        return Math.round(coins);
    }
}
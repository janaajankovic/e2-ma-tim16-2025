package com.example.habittrackerrpg.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.logic.BattleStats;
import com.example.habittrackerrpg.logic.CalculateLevelProgressUseCase;
import com.example.habittrackerrpg.logic.CalculatePpAwardUseCase;
import com.example.habittrackerrpg.logic.CalculateUserStatsUseCase;
import com.example.habittrackerrpg.logic.TitleHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileRepository {
    private static final String TAG = "ProfileRepository";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();

    public MutableLiveData<User> getUserLiveData() {
        if (mAuth.getCurrentUser() == null) return userLiveData;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                User user = snapshot.toObject(User.class);
                if (user != null) {
                    user.setId(snapshot.getId());
                    userLiveData.setValue(user);
                }
            }
        });
        return userLiveData;
    }

    public MutableLiveData<User> getUserById(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        if (userId == null) {
            userLiveData.setValue(null);
            return userLiveData;
        }
        db.collection("users").document(userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null && snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(snapshot.getId());
                            userLiveData.setValue(user);
                        }
                    } else {
                        userLiveData.setValue(null);
                    }
                });
        return userLiveData;
    }


    public void addXp(int xpToAdd, List<Task> allTasks, List<TaskInstance> allInstances) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection("users").document(uid);

        CalculateLevelProgressUseCase levelCalculator = new CalculateLevelProgressUseCase();
        CalculatePpAwardUseCase ppCalculator = new CalculatePpAwardUseCase();
        CalculateUserStatsUseCase statsCalculator = new CalculateUserStatsUseCase();

        db.runTransaction(transaction -> {
                    User currentUser = transaction.get(userDocRef).toObject(User.class);
                    if (currentUser == null) return null;

                    int oldLevel = currentUser.getLevel();
                    Date oldLevelUpTimestamp = currentUser.getLastLevelUpTimestamp();

                    long newXp = currentUser.getXp() + xpToAdd;
                    currentUser.setXp(newXp);

                    CalculateLevelProgressUseCase.LevelProgressResult newProgress = levelCalculator.execute(newXp);
                    int newLevel = newProgress.level;

                    if (newLevel > oldLevel) {
                        Log.d(TAG, "Level Up! Sa " + oldLevel + " na " + newLevel);

                        // --- LOGIKA ZA STATISTIKU ---
                        // Računamo statistiku za etapu koja se upravo završila
                        Date etapaEndDate = new Date(); // Kraj etape je sada
                        BattleStats lastStageStats = statsCalculator.execute(allTasks, allInstances, oldLevelUpTimestamp, etapaEndDate);
                        currentUser.setLastStageHitChance(lastStageStats.getHitChancePercentage());
                        // -----------------------------

                        currentUser.setLevel(newLevel);
                        currentUser.setTitle(TitleHelper.getTitleForLevel(newLevel));
                        currentUser.setLastLevelUpTimestamp(etapaEndDate);

                        long totalPpToAdd = 0;
                        for (int level = oldLevel + 1; level <= newLevel; level++) {
                            totalPpToAdd += ppCalculator.execute(level);
                        }
                        currentUser.setPp(currentUser.getPp() + totalPpToAdd);
                    }

                    transaction.set(userDocRef, currentUser);
                    return null;
                }).addOnSuccessListener(aVoid -> Log.d(TAG, "Transakcija uspešna!"))
                .addOnFailureListener(e -> Log.w(TAG, "Transakcija neuspešna.", e));
    }


    public void updateUser(User user) {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot update user. User not logged in.");
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating user profile", e));
    }

    public void addCoins(long amount) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection("users").document(uid);

        userDocRef.update("coins", FieldValue.increment(amount))
                .addOnSuccessListener(aVoid -> Log.d("ProfileRepository", "Coins updated successfully."))
                .addOnFailureListener(e -> Log.e("ProfileRepository", "Error updating coins", e));
    }

    public void updateUserAfterBossVictory(int defeatedBossLevel) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection("users").document(uid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("highestBossDefeatedLevel", defeatedBossLevel);
        updates.put("lastLevelUpTimestamp", new Date());

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d("ProfileRepository", "User boss progress updated."))
                .addOnFailureListener(e -> Log.e("ProfileRepository", "Error updating boss progress", e));
    }

    public void recordBossFightAttempt(int currentLevel) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection("users").document(uid);

        userDocRef.update("lastBossFightAttemptLevel", currentLevel)
                .addOnSuccessListener(aVoid -> Log.d("ProfileRepository", "User boss fight attempt level recorded."))
                .addOnFailureListener(e -> Log.e("ProfileRepository", "Error recording fight attempt", e));
    }


}
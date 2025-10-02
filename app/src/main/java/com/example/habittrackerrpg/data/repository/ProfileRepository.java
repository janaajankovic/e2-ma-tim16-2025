package com.example.habittrackerrpg.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.logic.CalculateLevelProgressUseCase;
import com.example.habittrackerrpg.logic.CalculatePpAwardUseCase;
import com.example.habittrackerrpg.logic.TitleHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
                userLiveData.setValue(snapshot.toObject(User.class));
            }
        });
        return userLiveData;
    }


    public void addXp(int xpToAdd) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection("users").document(uid);

        CalculateLevelProgressUseCase levelCalculator = new CalculateLevelProgressUseCase();
        CalculatePpAwardUseCase ppCalculator = new CalculatePpAwardUseCase();

        db.runTransaction(transaction -> {
                    User currentUser = transaction.get(userDocRef).toObject(User.class);
                    if (currentUser == null) return null;

                    int oldLevel = currentUser.getLevel();
                    long newXp = currentUser.getXp() + xpToAdd;
                    currentUser.setXp(newXp);

                    CalculateLevelProgressUseCase.LevelProgressResult newProgress = levelCalculator.execute(newXp);
                    int newLevel = newProgress.level;

                    if (newLevel > oldLevel) {
                        Log.d(TAG, "Level Up! Sa " + oldLevel + " na " + newLevel);
                        currentUser.setLevel(newLevel);
                        currentUser.setTitle(TitleHelper.getTitleForLevel(newLevel));

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

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
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
}
package com.example.habittrackerrpg.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.habittrackerrpg.data.model.Boss;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BossRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MutableLiveData<List<Boss>> allBossesLiveData = new MutableLiveData<>();

    public BossRepository() {
        fetchAllBosses();
    }
    public LiveData<List<Boss>> getAllBosses() {
        return allBossesLiveData;
    }

    public void addNewBoss(Boss boss) {
        if (boss == null) return;

        String documentId = String.valueOf(boss.getLevel());

        db.collection("bosses").document(documentId).set(boss)
                .addOnSuccessListener(aVoid -> Log.d("BossRepository", "New boss for level " + boss.getLevel() + " successfully created in DB."))
                .addOnFailureListener(e -> Log.e("BossRepository", "Error creating new boss in DB", e));
    }
    private void fetchAllBosses() {
        db.collection("bosses").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("BossRepository", "Error fetching bosses", error);
                return;
            }

            if (value != null) {
                List<Boss> bosses = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    Boss boss = doc.toObject(Boss.class);
                    bosses.add(boss);
                }
                allBossesLiveData.setValue(bosses);
                Log.d("BossRepository", "Fetched " + bosses.size() + " bosses.");
            }
        });
    }
}
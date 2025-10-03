package com.example.habittrackerrpg.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.habittrackerrpg.data.model.Alliance;
import com.example.habittrackerrpg.data.model.AllianceInvite;
import com.example.habittrackerrpg.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class AllianceRepository {

    private static final String TAG = "AllianceRepository";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ProfileRepository profileRepository;

    public AllianceRepository() {
        this.profileRepository = new ProfileRepository();
    }
    public void createAlliance(String allianceName, User leader) {
        String leaderId = mAuth.getCurrentUser().getUid();

        Alliance newAlliance = new Alliance(allianceName, leaderId, leader.getUsername(), leader.getAvatarId());
        WriteBatch batch = db.batch();

        DocumentReference newAllianceRef = db.collection("alliances").document();
        batch.set(newAllianceRef, newAlliance);

        DocumentReference userRef = db.collection("users").document(leaderId);
        batch.update(userRef, "allianceId", newAllianceRef.getId());

        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Alliance created successfully."))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating alliance", e));
    }
    public LiveData<Alliance> getUsersAlliance() {
        LiveData<User> currentUserLiveData = profileRepository.getUserLiveData();

        return Transformations.switchMap(currentUserLiveData, user -> {
            MutableLiveData<Alliance> allianceLiveData = new MutableLiveData<>();
            if (user != null && user.getAllianceId() != null && !user.getAllianceId().isEmpty()) {
                db.collection("alliances").document(user.getAllianceId())
                        .addSnapshotListener((snapshot, e) -> {
                            if (snapshot != null && snapshot.exists()) {
                                Alliance alliance = snapshot.toObject(Alliance.class);
                                if (alliance != null) {
                                    alliance.setId(snapshot.getId());
                                    allianceLiveData.setValue(alliance);
                                }
                            } else {
                                allianceLiveData.setValue(null);
                            }
                        });
            } else {
                allianceLiveData.setValue(null);
            }
            return allianceLiveData;
        });
    }

    public void sendAllianceInvite(String friendId, Alliance currentAlliance, String inviterUsername) {
        AllianceInvite invite = new AllianceInvite(currentAlliance.getId(), currentAlliance.getName(), inviterUsername, friendId);

        db.collection("users").document(friendId).collection("alliance_invites")
                .add(invite)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Alliance invite sent to " + friendId))
                .addOnFailureListener(e -> Log.e(TAG, "Error sending alliance invite", e));
    }

    public LiveData<List<AllianceInvite>> getPendingInvitesForAlliance(String allianceId) {
        MutableLiveData<List<AllianceInvite>> invitesLiveData = new MutableLiveData<>(new ArrayList<>());
        if (allianceId == null) {
            return invitesLiveData;
        }

        db.collectionGroup("alliance_invites")
                .whereEqualTo("allianceId", allianceId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error getting pending alliance invites.", e);
                        return;
                    }
                    if (snapshots != null) {
                        invitesLiveData.setValue(snapshots.toObjects(AllianceInvite.class));
                    }
                });
        return invitesLiveData;
    }
}
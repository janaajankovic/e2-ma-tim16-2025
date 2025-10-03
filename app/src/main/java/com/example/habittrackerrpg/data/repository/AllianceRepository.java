package com.example.habittrackerrpg.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.habittrackerrpg.data.model.Alliance;
import com.example.habittrackerrpg.data.model.AllianceInvite;
import com.example.habittrackerrpg.data.model.AllianceMember;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.logic.NotificationSender;
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

    public void sendAllianceInviteAndNotify(Context context, String friendId, Alliance currentAlliance, User currentUser) {
        AllianceInvite invite = new AllianceInvite(currentAlliance.getId(), currentAlliance.getName(), currentUser.getUsername(), friendId);

        db.collection("users").document(friendId).collection("alliance_invites")
                .add(invite)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Alliance invite successfully written to database with ID: " + docRef.getId());

                    // TEK SADA, KADA ZNAMO DA JE POZIVNICA KREIRANA, ŠALJEMO NOTIFIKACIJU
                    String title = "Alliance Invitation";
                    String message = currentUser.getUsername() + " invites you to join '" + currentAlliance.getName() + "'.";
                    NotificationSender.sendNotificationToUser(context, friendId, title, message, docRef.getId(), currentAlliance.getId());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error writing alliance invite to database", e));
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
    public LiveData<List<AllianceInvite>> getReceivedAllianceInvites() {
        MutableLiveData<List<AllianceInvite>> invitesLiveData = new MutableLiveData<>(new ArrayList<>());
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) return invitesLiveData;

        db.collection("users").document(uid).collection("alliance_invites")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error getting received alliance invites.", e);
                        return;
                    }
                    if (snapshots != null) {
                        invitesLiveData.setValue(snapshots.toObjects(AllianceInvite.class));
                    }
                });
        return invitesLiveData;
    }

    public void acceptAllianceInvite(AllianceInvite invite, User userOnProfile) {
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference allianceRef = db.collection("alliances").document(invite.getAllianceId());
        DocumentReference userRef = db.collection("users").document(uid);
        DocumentReference inviteRef = userRef.collection("alliance_invites").document(invite.getId());
        AllianceMember newMember = new AllianceMember(uid, userOnProfile.getUsername(), userOnProfile.getAvatarId());
        WriteBatch batch = db.batch();
        batch.update(allianceRef, "members." + uid, newMember);
        batch.update(userRef, "allianceId", invite.getAllianceId());
        batch.delete(inviteRef);
        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully accepted alliance invite."))
                .addOnFailureListener(e -> Log.e(TAG, "Error accepting alliance invite.", e));
    }

    public void declineAllianceInvite(AllianceInvite invite) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("alliance_invites").document(invite.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully declined alliance invite."));
    }
}
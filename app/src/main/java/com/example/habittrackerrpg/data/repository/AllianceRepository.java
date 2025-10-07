package com.example.habittrackerrpg.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.habittrackerrpg.data.model.Alliance;
import com.example.habittrackerrpg.data.model.AllianceInvite;
import com.example.habittrackerrpg.data.model.AllianceMember;
import com.example.habittrackerrpg.data.model.Message;
import com.example.habittrackerrpg.data.model.SpecialMission;
import com.example.habittrackerrpg.data.model.SpecialMissionProgress;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.logic.EndMissionWorker;
import com.example.habittrackerrpg.logic.NotificationSender;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AllianceRepository {

    private static final String TAG = "AllianceRepository";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ProfileRepository profileRepository;
    private final Context context;

    public AllianceRepository(Context context) {
        this.profileRepository = new ProfileRepository();
        this.context = context;
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

    public void acceptAllianceInvite(Context context, AllianceInvite invite, User userOnProfile) {
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
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully accepted alliance invite.");

                    db.collection("alliances").document(invite.getAllianceId()).get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String leaderId = doc.getString("leaderId");
                            String title = "New Member!";
                            String message = userOnProfile.getUsername() + " has joined your alliance '" + invite.getAllianceName() + "'.";
                            NotificationSender.sendSimpleNotification(context, leaderId, title, message);
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error accepting alliance invite.", e));
    }

    public void declineAllianceInvite(AllianceInvite invite) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("alliance_invites").document(invite.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully declined alliance invite."));
    }
    public void acceptInviteAndLeaveOldAlliance(Context context, AllianceInvite invite, User userOnProfile) {
        String uid = userOnProfile.getId();
        String oldAllianceId = userOnProfile.getAllianceId();

        if (oldAllianceId == null || oldAllianceId.isEmpty()) {
            acceptAllianceInvite(context, invite, userOnProfile);
            return;
        }

        DocumentReference newAllianceRef = db.collection("alliances").document(invite.getAllianceId());
        DocumentReference oldAllianceRef = db.collection("alliances").document(oldAllianceId);
        DocumentReference userRef = db.collection("users").document(uid);
        DocumentReference inviteRef = userRef.collection("alliance_invites").document(invite.getId());
        AllianceMember newMember = new AllianceMember(uid, userOnProfile.getUsername(), userOnProfile.getAvatarId());

        WriteBatch batch = db.batch();

        batch.update(oldAllianceRef, "members." + uid, com.google.firebase.firestore.FieldValue.delete());
        batch.update(newAllianceRef, "members." + uid, newMember);
        batch.update(userRef, "allianceId", invite.getAllianceId());
        batch.delete(inviteRef);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully left old alliance and joined the new one.");

                    db.collection("alliances").document(invite.getAllianceId()).get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String leaderId = doc.getString("leaderId");
                            String title = "New Member!";
                            String message = userOnProfile.getUsername() + " has joined your alliance '" + invite.getAllianceName() + "'.";
                            NotificationSender.sendSimpleNotification(context, leaderId, title, message);
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error switching alliances.", e));
    }
    public void disbandAllianceAndJoinNew(Context context, com.example.habittrackerrpg.data.model.AllianceInvite invite, com.example.habittrackerrpg.data.model.User leader, com.example.habittrackerrpg.data.model.Alliance oldAlliance) {
        String leaderId = leader.getId();
        String oldAllianceId = oldAlliance.getId();
        String newAllianceId = invite.getAllianceId();

        WriteBatch batch = db.batch();

        for (String memberId : oldAlliance.getMembers().keySet()) {
            if (!memberId.equals(leaderId)) {
                DocumentReference memberRef = db.collection("users").document(memberId);
                batch.update(memberRef, "allianceId", null);
            }
        }

        DocumentReference oldAllianceRef = db.collection("alliances").document(oldAllianceId);
        batch.delete(oldAllianceRef);

        DocumentReference leaderRef = db.collection("users").document(leaderId);
        batch.update(leaderRef, "allianceId", newAllianceId);

        DocumentReference newAllianceRef = db.collection("alliances").document(newAllianceId);
        AllianceMember leaderAsMember = new AllianceMember(leaderId, leader.getUsername(), leader.getAvatarId());
        batch.update(newAllianceRef, "members." + leaderId, leaderAsMember);

        DocumentReference inviteRef = leaderRef.collection("alliance_invites").document(invite.getId());
        batch.delete(inviteRef);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully disbanded old alliance and joined the new one.");

                    db.collection("alliances").document(newAllianceId).get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String newLeaderId = doc.getString("leaderId");
                            String title = "New Member!";
                            String message = leader.getUsername() + " has joined your alliance '" + invite.getAllianceName() + "'.";
                            NotificationSender.sendSimpleNotification(context, newLeaderId, title, message);
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error disbanding alliance.", e));
    }
    public void leaveAlliance(String userId, String allianceId) {
        WriteBatch batch = db.batch();

        DocumentReference allianceRef = db.collection("alliances").document(allianceId);
        batch.update(allianceRef, "members." + userId, com.google.firebase.firestore.FieldValue.delete());

        DocumentReference userRef = db.collection("users").document(userId);
        batch.update(userRef, "allianceId", null);

        batch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "User " + userId + " successfully left alliance " + allianceId));
    }
    public void disbandAlliance(Alliance alliance) {
        WriteBatch batch = db.batch();

        for (String memberId : alliance.getMembers().keySet()) {
            DocumentReference memberRef = db.collection("users").document(memberId);
            batch.update(memberRef, "allianceId", null);
        }

        DocumentReference allianceRef = db.collection("alliances").document(alliance.getId());
        batch.delete(allianceRef);

        batch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "Alliance " + alliance.getId() + " was successfully disbanded by leader."));
    }
    public LiveData<List<Message>> getChatMessages(String allianceId) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();

        db.collection("alliances").document(allianceId).collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        List<Message> messages = snapshots.toObjects(Message.class);
                        messagesLiveData.setValue(messages);
                    }
                });

        return messagesLiveData;
    }
    public void sendMessage(String allianceId, Message message) {
        db.collection("alliances").document(allianceId).collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message sent successfully!");

                    logMissionAction("ALLIANCE_MESSAGE", 4);

                })
                .addOnFailureListener(e -> Log.e(TAG, "Error sending message", e));
    }

    public void startSpecialMission(Alliance alliance) {
        if (alliance.getActiveMissionId() != null) {
            Log.w(TAG, "Alliance already has an active mission.");
            return;
        }

        int memberCount = alliance.getMembers().size();
        if (memberCount == 0) return;
        long bossHp = 100 * memberCount;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 14);
        Date endDate = calendar.getTime();

        SpecialMission mission = new SpecialMission(alliance.getId(), alliance.getLeaderId(), endDate, bossHp);

        WriteBatch batch = db.batch();
        DocumentReference missionRef = db.collection("specialMissions").document();
        batch.set(missionRef, mission);

        DocumentReference allianceRef = db.collection("alliances").document(alliance.getId());
        batch.update(allianceRef, "activeMissionId", missionRef.getId());

        for (AllianceMember member : alliance.getMembers().values()) {
            SpecialMissionProgress progress = new SpecialMissionProgress(member.getUserId(), member.getUsername());
            DocumentReference progressRef = missionRef.collection("progress").document(member.getUserId());
            batch.set(progressRef, progress);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Special mission started successfully!");

            // Zakazujemo Worker da završi misiju za 14 dana
            Data inputData = new Data.Builder().putString("MISSION_ID", missionRef.getId()).build();
            OneTimeWorkRequest endMissionWorkRequest = new OneTimeWorkRequest.Builder(EndMissionWorker.class)
                    .setInitialDelay(14, TimeUnit.DAYS)
                    .setInputData(inputData)
                    .build();
            WorkManager.getInstance(context).enqueue(endMissionWorkRequest);

            Log.d(TAG, "EndMissionWorker scheduled for mission: " + missionRef.getId());
        });
    }

    public void logMissionAction(String actionType, int damage) {
        String uid = mAuth.getCurrentUser().getUid();
        profileRepository.getUserLiveData().observeForever(user -> {
            if (user == null || user.getAllianceId() == null) return;
            db.collection("alliances").document(user.getAllianceId()).get().addOnSuccessListener(allianceDoc -> {
                String missionId = allianceDoc.getString("activeMissionId");
                if (missionId == null) return;

                DocumentReference progressRef = db.collection("specialMissions").document(missionId).collection("progress").document(uid);
                DocumentReference missionRef = db.collection("specialMissions").document(missionId);

                db.runTransaction(transaction -> {
                    SpecialMissionProgress progress = transaction.get(progressRef).toObject(SpecialMissionProgress.class);
                    if (progress == null) return null;

                    boolean shouldDealDamage = false;
                    switch (actionType) {
                        case "SHOP_PURCHASE":
                            if (progress.getShopPurchases() < 5) {
                                progress.setShopPurchases(progress.getShopPurchases() + 1);
                                shouldDealDamage = true;
                            }
                            break;
                        case "REGULAR_BOSS_HIT":
                            if (progress.getRegularBossHits() < 10) {
                                progress.setRegularBossHits(progress.getRegularBossHits() + 1);
                                shouldDealDamage = true;
                            }
                            break;
                        case "TASK_COMPLETION":
                            if (progress.getTaskCompletions() < 10) {
                                progress.setTaskCompletions(progress.getTaskCompletions() + 1);
                                shouldDealDamage = true;
                            }
                            break;
                        case "OTHER_TASK_COMPLETION":
                            if (progress.getOtherTaskCompletions() < 6) {
                                progress.setOtherTaskCompletions(progress.getOtherTaskCompletions() + 1);
                                shouldDealDamage = true;
                            }
                            break;
                        case "ALLIANCE_MESSAGE":
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String today = sdf.format(new Date());
                            if (!progress.getDailyMessageDates().contains(today)) {
                                progress.getDailyMessageDates().add(today);
                                shouldDealDamage = true;
                            }
                            break;
                    }

                    if (shouldDealDamage) {
                        progress.setTotalDamageDealt(progress.getTotalDamageDealt() + damage);
                        transaction.set(progressRef, progress);
                        transaction.update(missionRef, "currentBossHp", FieldValue.increment(-damage));
                    }
                    return null;
                });
            });
        });
    }

    public LiveData<SpecialMission> getMissionDetails(String missionId) {
        MutableLiveData<SpecialMission> liveData = new MutableLiveData<>();
        db.collection("specialMissions").document(missionId)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null && snapshot.exists()) {
                        liveData.setValue(snapshot.toObject(SpecialMission.class));
                    } else {
                        liveData.setValue(null);
                    }
                });
        return liveData;
    }

    public LiveData<List<SpecialMissionProgress>> getAllMembersProgress(String missionId) {
        MutableLiveData<List<SpecialMissionProgress>> liveData = new MutableLiveData<>();
        db.collection("specialMissions").document(missionId).collection("progress")
                .orderBy("totalDamageDealt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null) {
                        liveData.setValue(snapshots.toObjects(SpecialMissionProgress.class));
                    }
                });
        return liveData;
    }

    public LiveData<SpecialMissionProgress> getMyProgress(String missionId) {
        String uid = mAuth.getCurrentUser().getUid();
        MutableLiveData<SpecialMissionProgress> liveData = new MutableLiveData<>();
        db.collection("specialMissions").document(missionId).collection("progress").document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null && snapshot.exists()) {
                        liveData.setValue(snapshot.toObject(SpecialMissionProgress.class));
                    }
                });
        return liveData;
    }
}
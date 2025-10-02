package com.example.habittrackerrpg.data.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.data.model.FriendRequest;
import com.example.habittrackerrpg.data.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;

public class FriendsRepository {

    private static final String TAG = "FriendsRepository";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<User>> searchUsersByUsername(String query) {
        MutableLiveData<List<User>> searchResults = new MutableLiveData<>();
        if (query.isEmpty() || query.length() < 3) { // Pretraga kreće tek nakon 3 karaktera
            searchResults.setValue(new ArrayList<>());
            return searchResults;
        }

        db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        user.setId(doc.getId());
                        users.add(user);
                    }
                    searchResults.setValue(users);
                })
                .addOnFailureListener(e -> Log.e(TAG, "User search failed", e));

        return searchResults;
    }

    public void sendFriendRequest(String receiverId, User senderData) {
        FirebaseUser currentUserAuth = mAuth.getCurrentUser();
        if (currentUserAuth == null) return;
        String senderId = currentUserAuth.getUid();

        FriendRequest request = new FriendRequest(senderId, senderData.getUsername(), senderData.getAvatarId(), receiverId);

        db.collection("users").document(receiverId)
                .collection("friend_requests").add(request)
                .addOnSuccessListener(doc -> Log.d(TAG, "Friend request sent."))
                .addOnFailureListener(e -> Log.e(TAG, "Error sending friend request", e));
    }

    public LiveData<List<FriendRequest>> getFriendRequests() {
        MutableLiveData<List<FriendRequest>> requestsLiveData = new MutableLiveData<>();
        FirebaseUser currentUserAuth = mAuth.getCurrentUser();
        if (currentUserAuth == null) return requestsLiveData;
        String uid = currentUserAuth.getUid();

        db.collection("users").document(uid).collection("friend_requests")
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null) {
                        List<FriendRequest> requests = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            FriendRequest req = doc.toObject(FriendRequest.class);
                            if (req != null) {
                                req.setRequestId(doc.getId());
                                requests.add(req);
                            }
                        }
                        requestsLiveData.setValue(requests);
                    }
                });
        return requestsLiveData;
    }

    public LiveData<List<Friend>> getFriends() {
        MutableLiveData<List<Friend>> friendsLiveData = new MutableLiveData<>();
        FirebaseUser currentUserAuth = mAuth.getCurrentUser();
        if (currentUserAuth == null) return friendsLiveData;
        String uid = currentUserAuth.getUid();

        db.collection("users").document(uid).collection("friends")
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null) {
                        friendsLiveData.setValue(snapshots.toObjects(Friend.class));
                    }
                });
        return friendsLiveData;
    }

    public void acceptFriendRequest(FriendRequest request) {
        FirebaseUser currentUserAuth = mAuth.getCurrentUser();
        if (currentUserAuth == null) return;
        String currentUserUid = currentUserAuth.getUid();
        String friendUid = request.getSenderId();

        db.collection("users").document(currentUserUid).get().addOnSuccessListener(doc -> {
            User currentUserData = doc.toObject(User.class);
            if (currentUserData == null) return;

            Friend newFriendForCurrentUser = new Friend(friendUid, request.getSenderUsername(), request.getSenderAvatarId());
            Friend newFriendForSender = new Friend(currentUserUid, currentUserData.getUsername(), currentUserData.getAvatarId());

            WriteBatch batch = db.batch();

            batch.set(db.collection("users").document(currentUserUid).collection("friends").document(friendUid), newFriendForCurrentUser);
            batch.set(db.collection("users").document(friendUid).collection("friends").document(currentUserUid), newFriendForSender);
            batch.delete(db.collection("users").document(currentUserUid).collection("friend_requests").document(request.getRequestId()));

            batch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "Friend request accepted."));
        });
    }

    public void declineFriendRequest(FriendRequest request) {
        FirebaseUser currentUserAuth = mAuth.getCurrentUser();
        if (currentUserAuth == null) return;
        String currentUserUid = currentUserAuth.getUid();

        db.collection("users").document(currentUserUid)
                .collection("friend_requests").document(request.getRequestId())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Friend request declined."));
    }

    public LiveData<List<FriendRequest>> getSentFriendRequests() {
        MutableLiveData<List<FriendRequest>> requestsLiveData = new MutableLiveData<>();
        String uid = mAuth.getCurrentUser().getUid();

        db.collectionGroup("friend_requests")
                .whereEqualTo("senderId", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null) {
                        requestsLiveData.setValue(snapshots.toObjects(FriendRequest.class));
                    }
                });
        return requestsLiveData;
    }
}
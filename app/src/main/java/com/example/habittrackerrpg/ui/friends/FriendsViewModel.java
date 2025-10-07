package com.example.habittrackerrpg.ui.friends;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import android.content.Context;
import com.example.habittrackerrpg.data.model.Alliance;
import com.example.habittrackerrpg.data.model.AllianceInvite;
import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.data.model.FriendRequest;
import com.example.habittrackerrpg.data.model.Message;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.AllianceRepository;
import com.example.habittrackerrpg.data.repository.FriendsRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.logic.Event;
import com.example.habittrackerrpg.logic.NotificationSender;
import com.example.habittrackerrpg.logic.RelationshipStatus;
import com.example.habittrackerrpg.logic.UserSearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FriendsViewModel extends AndroidViewModel {

    private final FriendsRepository friendsRepository;
    private final ProfileRepository profileRepository;
    private final AllianceRepository allianceRepository;

    private final LiveData<List<Friend>> friendsList;
    private final LiveData<List<FriendRequest>> friendRequests;
    private final LiveData<List<FriendRequest>> sentFriendRequests;
    private final LiveData<User> currentUserData;
    private final LiveData<Alliance> currentAlliance;

    private final MediatorLiveData<List<UserSearchResult>> searchResults = new MediatorLiveData<>();
    private LiveData<List<User>> currentSearchSource = null;
    private final MutableLiveData<Event<String>> toastMessage = new MutableLiveData<>();
    private final LiveData<List<AllianceInvite>> pendingAllianceInvites;
    private final LiveData<List<Message>> chatMessages;

    public FriendsViewModel(@NonNull Application application) {
        super(application);
        this.friendsRepository = new FriendsRepository();
        this.profileRepository = new ProfileRepository();
        this.allianceRepository = new AllianceRepository(application.getApplicationContext());

        this.friendsList = friendsRepository.getFriends();
        this.friendRequests = friendsRepository.getFriendRequests();
        this.sentFriendRequests = friendsRepository.getSentFriendRequests();
        this.currentUserData = profileRepository.getUserLiveData();
        this.currentAlliance = allianceRepository.getUsersAlliance();

        searchResults.addSource(currentUserData, user -> combineAllData());
        searchResults.addSource(friendsList, friends -> combineAllData());
        searchResults.addSource(sentFriendRequests, sentRequests -> combineAllData());
        this.pendingAllianceInvites = Transformations.switchMap(currentAlliance, alliance -> {
            if (alliance == null) {
                MutableLiveData<List<AllianceInvite>> emptyResult = new MutableLiveData<>();
                emptyResult.setValue(new ArrayList<>());
                return emptyResult;
            }
            return allianceRepository.getPendingInvitesForAlliance(alliance.getId());
        });
        this.chatMessages = Transformations.switchMap(currentAlliance, alliance -> {
            if (alliance == null || alliance.getId() == null) {
                MutableLiveData<List<Message>> emptyResult = new MutableLiveData<>();
                emptyResult.setValue(new ArrayList<>());
                return emptyResult;
            }
            return allianceRepository.getChatMessages(alliance.getId());
        });
    }

    public LiveData<List<Message>> getChatMessages() {
        return chatMessages;
    }

    public LiveData<List<AllianceInvite>> getPendingAllianceInvites() {
        return pendingAllianceInvites;
    }
    public LiveData<List<Friend>> getFriendsList() { return friendsList; }
    public LiveData<List<FriendRequest>> getFriendRequests() { return friendRequests; }
    public LiveData<List<UserSearchResult>> getSearchResults() { return searchResults; }
    public LiveData<Alliance> getCurrentAlliance() { return currentAlliance; }
    public LiveData<Event<String>> getToastMessage() { return toastMessage; }
    public void searchUsers(String query) {
        if (currentSearchSource != null) {
            searchResults.removeSource(currentSearchSource);
        }
        currentSearchSource = friendsRepository.searchUsersByUsername(query);
        searchResults.addSource(currentSearchSource, users -> combineAllData());
    }

    private void combineAllData() {
        String TAG = "MyDebug-ViewModel";
        Log.d(TAG, "--- combineAllData POKRENUT ---");
        List<User> users = (currentSearchSource != null) ? currentSearchSource.getValue() : new ArrayList<>();
        User currentUser = currentUserData.getValue();
        List<Friend> friends = friendsList.getValue();
        List<FriendRequest> sentRequests = sentFriendRequests.getValue();

        Log.d(TAG, "Status podataka: [Users: " + (users != null ? users.size() : "null") +
                "], [CurrentUser: " + (currentUser != null ? currentUser.getUsername() : "null") +
                "], [Friends: " + (friends != null ? friends.size() : "null") +
                "], [SentRequests: " + (sentRequests != null ? sentRequests.size() : "null") + "]");

        if (users == null || currentUser == null || friends == null || sentRequests == null) {
            Log.w(TAG, "Jedan od izvora podataka je NULL. Prekidam kombinovanje.");
            return;
        }

        if (users == null || currentUser == null || friends == null || sentRequests == null) {
            // Ako je upit prazan, osiguraj da je i lista prazna
            if (currentSearchSource == null || (currentSearchSource.getValue() != null && currentSearchSource.getValue().isEmpty())) {
                searchResults.setValue(new ArrayList<>());
            }
            return;
        }

        List<String> friendIds = friends.stream().map(Friend::getUserId).collect(Collectors.toList());
        List<String> sentRequestReceiverIds = sentRequests.stream().map(FriendRequest::getReceiverId).collect(Collectors.toList());

        List<UserSearchResult> resultsWithStatus = new ArrayList<>();
        for (User user : users) {
            if (user.getId().equals(currentUser.getId())) {
                continue;
            }

            RelationshipStatus status;
            if (friendIds.contains(user.getId())) {
                status = RelationshipStatus.FRIENDS;
            } else if (sentRequestReceiverIds.contains(user.getId())) {
                status = RelationshipStatus.REQUEST_SENT;
            } else {
                status = RelationshipStatus.NONE;
            }
            resultsWithStatus.add(new UserSearchResult(user, status));
        }
        Log.d(TAG, "Kombinovanje ZAVRŠENO. Šaljem na UI listu od: " + resultsWithStatus.size() + " rezultata.");
        searchResults.setValue(resultsWithStatus);
    }

    public void sendFriendRequest(String receiverId) {
        User sender = currentUserData.getValue();
        if (sender != null) {
            friendsRepository.sendFriendRequest(receiverId, sender);
        }
    }

    public void acceptFriendRequest(FriendRequest request) {
        friendsRepository.acceptFriendRequest(request);
    }

    public void declineFriendRequest(FriendRequest request) {
        friendsRepository.declineFriendRequest(request);
    }
    public void createAlliance(String allianceName) {
        if (allianceName == null || allianceName.trim().isEmpty() || allianceName.length() < 3) {
            toastMessage.setValue(new Event<>("Alliance name must be at least 3 characters long."));
            return;
        }
        User currentUser = currentUserData.getValue();
        if (currentUser == null) {
            toastMessage.setValue(new Event<>("Cannot create alliance: User data not loaded."));
            return;
        }
        allianceRepository.createAlliance(allianceName, currentUser);
        toastMessage.setValue(new Event<>("Alliance '" + allianceName + "' created!"));
    }
    public void sendAllianceInvite(Context context, Friend friendToInvite) {
        Alliance alliance = currentAlliance.getValue();
        User currentUser = currentUserData.getValue();

        if (alliance == null || currentUser == null) {
            toastMessage.setValue(new Event<>("Error: Data not ready. Please try again."));
            return;
        }

        allianceRepository.sendAllianceInviteAndNotify(context, friendToInvite.getUserId(), alliance, currentUser);
    }
    public LiveData<User> getCurrentUserData() {
        return currentUserData;
    }
    public void onLeaveAllianceClicked() {
        User currentUser = currentUserData.getValue();
        Alliance currentAlliance = this.currentAlliance.getValue();

        if (currentUser != null && currentAlliance != null) {
            allianceRepository.leaveAlliance(currentUser.getId(), currentAlliance.getId());
            toastMessage.setValue(new Event<>("You have left the alliance."));
        } else {
            toastMessage.setValue(new Event<>("Error: Could not leave alliance."));
        }
    }

    public void onDisbandAllianceClicked() {
        Alliance currentAlliance = this.currentAlliance.getValue();

        if (currentAlliance != null) {
            allianceRepository.disbandAlliance(currentAlliance);
            toastMessage.setValue(new Event<>("Alliance has been disbanded."));
        } else {
            toastMessage.setValue(new Event<>("Error: Could not disband alliance."));
        }
    }
    public void sendMessage(String text) {
        User currentUser = currentUserData.getValue();
        Alliance currentAlliance = this.currentAlliance.getValue();

        if (currentUser != null && currentAlliance != null && text != null && !text.isEmpty()) {
            Message message = new Message(text, currentUser.getId(), currentUser.getUsername());
            allianceRepository.sendMessage(currentAlliance.getId(), message);
        }
    }
}
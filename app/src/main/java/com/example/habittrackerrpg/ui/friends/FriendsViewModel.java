package com.example.habittrackerrpg.ui.friends;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.data.model.FriendRequest;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.FriendsRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.logic.RelationshipStatus;
import com.example.habittrackerrpg.logic.UserSearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FriendsViewModel extends ViewModel {

    private final FriendsRepository friendsRepository;
    private final ProfileRepository profileRepository;

    private final LiveData<List<Friend>> friendsList;
    private final LiveData<List<FriendRequest>> friendRequests;
    private final LiveData<List<FriendRequest>> sentFriendRequests;
    private final LiveData<User> currentUserData;

    private final MediatorLiveData<List<UserSearchResult>> searchResults = new MediatorLiveData<>();
    private LiveData<List<User>> currentSearchSource = null;

    public FriendsViewModel() {
        this.friendsRepository = new FriendsRepository();
        this.profileRepository = new ProfileRepository();

        this.friendsList = friendsRepository.getFriends();
        this.friendRequests = friendsRepository.getFriendRequests();
        this.sentFriendRequests = friendsRepository.getSentFriendRequests();
        this.currentUserData = profileRepository.getUserLiveData();

        searchResults.addSource(currentUserData, user -> combineAllData());
        searchResults.addSource(friendsList, friends -> combineAllData());
        searchResults.addSource(sentFriendRequests, sentRequests -> combineAllData());
    }

    public LiveData<List<Friend>> getFriendsList() { return friendsList; }
    public LiveData<List<FriendRequest>> getFriendRequests() { return friendRequests; }
    public LiveData<List<UserSearchResult>> getSearchResults() { return searchResults; }

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
}
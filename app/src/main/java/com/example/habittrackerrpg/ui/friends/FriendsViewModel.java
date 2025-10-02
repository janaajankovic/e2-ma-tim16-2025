package com.example.habittrackerrpg.ui.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.data.model.FriendRequest;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.FriendsRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import java.util.List;

public class FriendsViewModel extends ViewModel {

    public static class RelatedData {
        public final List<Friend> friends;
        public final List<FriendRequest> sentRequests;
        public final User currentUser;

        RelatedData(List<Friend> friends, List<FriendRequest> sentRequests, User currentUser) {
            this.friends = friends;
            this.sentRequests = sentRequests;
            this.currentUser = currentUser;
        }
    }

    private final FriendsRepository friendsRepository;
    private final ProfileRepository profileRepository;

    private final LiveData<List<Friend>> friendsList;
    private final LiveData<List<FriendRequest>> friendRequests;
    private final LiveData<List<FriendRequest>> sentFriendRequests;
    private final LiveData<User> currentUserData;

    private final MediatorLiveData<List<User>> searchResults = new MediatorLiveData<>();
    private LiveData<List<User>> currentSearchSource;

    private final MediatorLiveData<RelatedData> relatedData = new MediatorLiveData<>();

    public FriendsViewModel() {
        this.friendsRepository = new FriendsRepository();
        this.profileRepository = new ProfileRepository();

        this.friendsList = friendsRepository.getFriends();
        this.friendRequests = friendsRepository.getFriendRequests();
        this.sentFriendRequests = friendsRepository.getSentFriendRequests();
        this.currentUserData = profileRepository.getUserLiveData();

        relatedData.addSource(friendsList, friends -> combineRelatedData());
        relatedData.addSource(sentFriendRequests, requests -> combineRelatedData());
        relatedData.addSource(currentUserData, user -> combineRelatedData());
    }

    private void combineRelatedData() {
        List<Friend> friends = friendsList.getValue();
        List<FriendRequest> sent = sentFriendRequests.getValue();
        User user = currentUserData.getValue();

        if (friends != null && sent != null && user != null) {
            relatedData.setValue(new RelatedData(friends, sent, user));
        }
    }

    public LiveData<List<Friend>> getFriendsList() { return friendsList; }
    public LiveData<List<FriendRequest>> getFriendRequests() { return friendRequests; }
    public LiveData<List<User>> getSearchResults() { return searchResults; }
    public LiveData<RelatedData> getRelatedData() { return relatedData; }

    public void searchUsers(String query) {
        if (currentSearchSource != null) {
            searchResults.removeSource(currentSearchSource);
        }
        currentSearchSource = friendsRepository.searchUsersByUsername(query);
        searchResults.addSource(currentSearchSource, users -> searchResults.setValue(users));
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
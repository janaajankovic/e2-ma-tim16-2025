package com.example.habittrackerrpg.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.data.model.FriendRequest;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.AuthRepository;
import com.example.habittrackerrpg.data.repository.FriendsRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.logic.CalculateLevelProgressUseCase;
import com.example.habittrackerrpg.data.repository.EquipmentRepository;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.logic.Event;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private AuthRepository authRepository;
    private LiveData<User> userProfileData;
    private final EquipmentRepository equipmentRepository;
    private ProfileRepository profileRepository;
    private final FriendsRepository friendsRepository;
    private CalculateLevelProgressUseCase calculateLevelProgressUseCase;
    private LiveData<User> userLiveData;
    private LiveData<CalculateLevelProgressUseCase.LevelProgressResult> levelProgressLiveData;
    private final LiveData<List<UserEquipment>> userInventory;
    private LiveData<List<EquipmentItem>> shopItems;

    private final MutableLiveData<String> displayedUserId = new MutableLiveData<>();
    private final LiveData<User> displayedUserData;
    private final LiveData<List<Friend>> friendsList;
    private final LiveData<List<FriendRequest>> sentFriendRequests;
    private final LiveData<User> currentUserData;
    private final MutableLiveData<Event<String>> toastMessage = new MutableLiveData<>();
    public ProfileViewModel() {
        authRepository = new AuthRepository();
        userProfileData = authRepository.getCurrentUser();
        this.profileRepository = new ProfileRepository();
        this.friendsRepository = new FriendsRepository();
        this.calculateLevelProgressUseCase = new CalculateLevelProgressUseCase();
        this.userLiveData = profileRepository.getUserLiveData();

        this.levelProgressLiveData = Transformations.map(userLiveData, user -> {
            if (user != null) {
                return calculateLevelProgressUseCase.execute(user.getXp());
            }
            return null;
        });
        this.equipmentRepository = new EquipmentRepository();

        this.shopItems = equipmentRepository.getShopItems();
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        userInventory = Transformations.switchMap(displayedUserId, userId -> {
            if (userId == null) {
                return equipmentRepository.getUserInventory(currentUid);
            } else {
                return equipmentRepository.getUserInventory(userId);
            }
        });

        this.friendsList = friendsRepository.getFriends();
        this.sentFriendRequests = friendsRepository.getSentFriendRequests();
        this.currentUserData = profileRepository.getUserById(currentUid);
        displayedUserData = Transformations.switchMap(displayedUserId, userId -> {
            if (userId == null) {
                return profileRepository.getUserById(currentUid);
            } else {
                return profileRepository.getUserById(userId);
            }
        });
    }

    public LiveData<User> getDisplayedUserData() {
        return displayedUserData;
    }

    public LiveData<List<Friend>> getFriendsList() { return friendsList; }
    public LiveData<List<FriendRequest>> getSentFriendRequests() { return sentFriendRequests; }
    public LiveData<Event<String>> getToastMessage() { return toastMessage; }

    public LiveData<User> getUserProfileData() {
        return userProfileData;
    }

    public void sendFriendRequest(String receiverId) {
        User sender = currentUserData.getValue();
        if (sender != null) {
            friendsRepository.sendFriendRequest(receiverId, sender);
            toastMessage.setValue(new Event<>("Friend request sent!"));
        } else {
            toastMessage.setValue(new Event<>("Error: Your user data is not loaded yet."));
        }
    }

    public void logoutUser() {
        authRepository.logoutUser();
    }
    public LiveData<User> getUser() {
        return userLiveData;
    }

    public LiveData<CalculateLevelProgressUseCase.LevelProgressResult> getLevelProgress() {
        return levelProgressLiveData;
    }

    public void addXpForTesting(int xpToAdd) {
        profileRepository.addXp(xpToAdd);
    }
    public LiveData<List<UserEquipment>> getUserInventory() {
        return userInventory;
    }

    public LiveData<List<EquipmentItem>> getShopItems() {
        return shopItems;
    }
    public void loadUser(String userId) {
        if (userId == null) {
            String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            displayedUserId.setValue(currentUid);
        } else {
            displayedUserId.setValue(userId);
        }
    }
}

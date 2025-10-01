package com.example.habittrackerrpg.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.AuthRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.logic.CalculateLevelProgressUseCase;
import com.example.habittrackerrpg.data.repository.EquipmentRepository;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.example.habittrackerrpg.data.model.EquipmentItem;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private AuthRepository authRepository;
    private LiveData<User> userProfileData;
    private ProfileRepository profileRepository;
    private CalculateLevelProgressUseCase calculateLevelProgressUseCase;
    private LiveData<User> userLiveData;
    private LiveData<CalculateLevelProgressUseCase.LevelProgressResult> levelProgressLiveData;
    private EquipmentRepository equipmentRepository;
    private LiveData<List<UserEquipment>> userInventory;
    private LiveData<List<EquipmentItem>> shopItems;

    public ProfileViewModel() {
        authRepository = new AuthRepository();
        userProfileData = authRepository.getCurrentUser();
        this.profileRepository = new ProfileRepository();
        this.calculateLevelProgressUseCase = new CalculateLevelProgressUseCase();
        this.userLiveData = profileRepository.getUserLiveData();

        this.levelProgressLiveData = Transformations.map(userLiveData, user -> {
            if (user != null) {
                return calculateLevelProgressUseCase.execute(user.getXp());
            }
            return null;
        });
        this.equipmentRepository = new EquipmentRepository();
        this.userInventory = equipmentRepository.getUserInventory();
        this.shopItems = equipmentRepository.getShopItems();
    }

    public LiveData<User> getUserProfileData() {
        return userProfileData;
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
}

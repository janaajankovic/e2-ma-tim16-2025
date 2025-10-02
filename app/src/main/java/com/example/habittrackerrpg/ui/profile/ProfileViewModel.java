package com.example.habittrackerrpg.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.AuthRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.logic.CalculateLevelProgressUseCase;

public class ProfileViewModel extends ViewModel {
    private AuthRepository authRepository;
    private LiveData<User> userProfileData;
    private ProfileRepository profileRepository;
    private CalculateLevelProgressUseCase calculateLevelProgressUseCase;
    private LiveData<User> userLiveData;
    private LiveData<CalculateLevelProgressUseCase.LevelProgressResult> levelProgressLiveData;


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
        //profileRepository.addXp(xpToAdd);
    }
}

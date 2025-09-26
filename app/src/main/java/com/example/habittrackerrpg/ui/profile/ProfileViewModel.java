package com.example.habittrackerrpg.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.AuthRepository;

public class ProfileViewModel extends ViewModel {
    private AuthRepository authRepository;
    private LiveData<User> userProfileData;

    public ProfileViewModel() {
        authRepository = new AuthRepository();
        userProfileData = authRepository.getCurrentUser();
    }

    public LiveData<User> getUserProfileData() {
        return userProfileData;
    }

    public void logoutUser() {
        authRepository.logoutUser();
    }
}

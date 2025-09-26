package com.example.habittrackerrpg.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private AuthRepository authRepository;
    public MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    public MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    private final MutableLiveData<Boolean> _isUnverifiedAccount = new MutableLiveData<>();
    public LiveData<Boolean> isUnverifiedAccount = _isUnverifiedAccount;


    public AuthViewModel() {
        authRepository = new AuthRepository();
    }

    public void registerUser(String email, String password, String username, String avatarId) {
        authRepository.createUser(email, password, username, avatarId, registrationSuccess);
    }

    public void loginUser(String email, String password) {
        authRepository.loginUser(email, password, loginSuccess, (MutableLiveData<Boolean>) _isUnverifiedAccount);
    }


    public void logoutUser() {
        authRepository.logoutUser();
    }
}

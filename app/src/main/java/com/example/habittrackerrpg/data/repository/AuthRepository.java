package com.example.habittrackerrpg.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habittrackerrpg.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.onesignal.OneSignal;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void createUser(String email, String password, String username, String avatarId, MutableLiveData<Boolean> success) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "FirebaseAuth: Korisnik uspešno kreiran.");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification();
                            saveUserData(firebaseUser, username, avatarId, success);
                        }
                    } else {
                        Log.w(TAG, "FirebaseAuth: Neuspešna registracija.", task.getException());
                        success.setValue(false);
                    }
                });
    }

    private void saveUserData(FirebaseUser firebaseUser, String username, String avatarId, MutableLiveData<Boolean> success) {
        String uid = firebaseUser.getUid();
        User newUser = new User(username, avatarId);

        db.collection("users").document(uid).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore: Korisnik uspešno sačuvan.");
                    success.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore: Greška pri čuvanju korisnika.", e);
                    success.setValue(false);
                });
    }
    public void loginUser(String email, String password, MutableLiveData<Boolean> success, MutableLiveData<Boolean> isUnverifiedAccount) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                OneSignal.login(user.getUid());
                                success.setValue(true);
                                isUnverifiedAccount.setValue(false);
                            } else {
                                success.setValue(false);
                                isUnverifiedAccount.setValue(true); // nije verifikovan
                            }
                        } else {
                            success.setValue(false);
                            isUnverifiedAccount.setValue(false);
                        }
                    } else {
                        success.setValue(false);
                        isUnverifiedAccount.setValue(false); // pogrešna šifra/email
                    }
                });
    }

    public void logoutUser() {
        OneSignal.logout();
        mAuth.signOut();
    }

    public LiveData<User> getCurrentUser() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        userLiveData.setValue(user);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Greška pri dohvatanju podataka o korisniku.", e);
                        userLiveData.setValue(null);
                    });
        } else {
            userLiveData.setValue(null);
        }

        return userLiveData;
    }
}
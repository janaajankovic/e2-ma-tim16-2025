package com.example.habittrackerrpg.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.databinding.FragmentRegisterBinding;

import java.util.ArrayList;
import java.util.List;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel authViewModel;

    private List<ImageView> avatarImageViews;
    private String selectedAvatar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupAvatarSelection();

        authViewModel.registrationSuccess.observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                Toast.makeText(getContext(), "Registration successful! Please check your email.", Toast.LENGTH_LONG).show();
                requireActivity().getSupportFragmentManager().popBackStack(); // vrati na login
            } else {
                Toast.makeText(getContext(), "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonRegister.setOnClickListener(v -> {
            String username = binding.editTextUsername.getText().toString().trim();
            String email = binding.editTextEmail.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString();
            String confirmPassword = binding.editTextConfirmPassword.getText().toString();

            // resetujemo prethodnu grešku
            binding.textErrorPassword.setVisibility(View.GONE);

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || selectedAvatar == null) {
                Toast.makeText(getContext(), "All fields and avatar are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                binding.textErrorPassword.setText(getString(R.string.error_password_mismatch));
                binding.textErrorPassword.setVisibility(View.VISIBLE);
                return;
            }

            authViewModel.registerUser(email, password, username, selectedAvatar);
        });

    }

    private void setupAvatarSelection() {
        avatarImageViews = new ArrayList<>();
        avatarImageViews.add(binding.avatar1);
        avatarImageViews.add(binding.avatar2);
        avatarImageViews.add(binding.avatar3);
        avatarImageViews.add(binding.avatar4);
        avatarImageViews.add(binding.avatar5);

        for (ImageView avatar : avatarImageViews) {
            avatar.setOnClickListener(v -> handleAvatarClick(avatar));
        }
    }

    private void handleAvatarClick(ImageView clickedAvatar) {
        int padding_in_px = (int) (5 * getResources().getDisplayMetrics().density);

        for (ImageView avatar : avatarImageViews) {
            avatar.setPadding(0, 0, 0, 0);
        }

        clickedAvatar.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
        selectedAvatar = getResources().getResourceEntryName(clickedAvatar.getId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

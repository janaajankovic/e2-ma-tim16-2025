package com.example.habittrackerrpg.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        profileViewModel.getUserProfileData().observe(getViewLifecycleOwner(), this::updateUI);

        binding.buttonLogout.setOnClickListener(v -> {
            profileViewModel.logoutUser();
            // TODO: navigate to AuthenticationActivity or Login screen
        });
    }

    private void updateUI(User user) {
        binding.textViewUsername.setText(user.getUsername());
        binding.textViewLevel.setText(getString(R.string.level_text, user.getLevel()));
        binding.textViewXp.setText(getString(R.string.xp_text, user.getXp()));
        binding.textViewPp.setText(getString(R.string.pp_text, user.getPp()));
        binding.textViewCoins.setText(getString(R.string.coins_text, user.getCoins()));
        binding.textViewTitle.setText(user.getTitle());

        String avatarId = user.getAvatarId();
        if (!avatarId.contains("_")) {
            avatarId = avatarId.replace("avatar", "avatar_");
        }

        int avatarResId = getResources().getIdentifier(
                avatarId, "drawable", requireContext().getPackageName()
        );

        if (avatarResId != 0) {
            binding.imageViewAvatar.setImageResource(avatarResId);
        } else {
            binding.imageViewAvatar.setImageResource(R.drawable.avatar_1);
            Log.e("ProfileFragment", "Avatar resource not found: " + avatarId);
        }

        binding.textViewBadges.setText(getString(R.string.badges_text, "N/А"));
        binding.textViewEquipment.setText(getString(R.string.equipment_text, "N/А"));
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

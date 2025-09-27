package com.example.habittrackerrpg.ui.profile;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittrackerrpg.MainActivity;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.databinding.FragmentProfileBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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

        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        profileViewModel.getUserProfileData().observe(getViewLifecycleOwner(), this::updateUI);
        profileViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.textViewUsername.setText(user.getUsername());
                binding.textViewTitle.setText(user.getTitle());
                binding.textViewLevel.setText("Level: " + user.getLevel());
                binding.textViewXp.setText("XP: " + user.getXp());
                binding.textViewPp.setText("PP: " + user.getPp());
                binding.textViewCoins.setText("Coins: " + user.getCoins());
            }
        });

        // Privremeno dugme za testiranje dodavanja XP-a
        binding.buttonLogout.setText("Add 50 XP (Test)");
        binding.buttonLogout.setOnClickListener(v -> {
            profileViewModel.addXpForTesting(50);
        });

    }

    private void updateUI(User user) {
        binding.textViewUsername.setText(user.getUsername());
        binding.textViewLevel.setText(getString(R.string.level_text, user.getLevel()));
        binding.textViewXp.setText(getString(R.string.xp_text, user.getXp()));
        binding.textViewPp.setText(getString(R.string.pp_text, user.getPp()));
        binding.textViewCoins.setText(getString(R.string.coins_text, user.getCoins()));
        binding.textViewTitle.setText(user.getTitle());

        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(user.getUsername(), BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            binding.imageViewQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

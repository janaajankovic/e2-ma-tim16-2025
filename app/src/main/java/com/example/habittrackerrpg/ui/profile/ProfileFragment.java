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
import androidx.navigation.Navigation;

import com.example.habittrackerrpg.MainActivity;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.data.model.FriendRequest;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.example.habittrackerrpg.databinding.FragmentProfileBinding;
import com.example.habittrackerrpg.logic.AvatarHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private InventoryAdapter inventoryAdapter;

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
        String userId = getArguments() != null ? getArguments().getString("userId") : null;
        profileViewModel.loadUser(userId);
        setupRecyclerView();
        setupObservers();

    }

    private void setupRecyclerView() {
        inventoryAdapter = new InventoryAdapter();
        binding.recyclerViewInventory.setAdapter(inventoryAdapter);
        inventoryAdapter.setOnItemClickListener(userEquipment -> {
            Bundle bundle = new Bundle();
            bundle.putString("userEquipmentId", userEquipment.getId());

            Navigation.findNavController(requireView()).navigate(R.id.action_profile_to_equipmentDetail, bundle);
        });
    }

    private void setupObservers() {
        profileViewModel.getDisplayedUserData().observe(getViewLifecycleOwner(), this::updateUI);
        profileViewModel.getFriendsList().observe(getViewLifecycleOwner(), friends -> {
            updateUI(profileViewModel.getDisplayedUserData().getValue());
        });


        profileViewModel.getUserInventory().observe(getViewLifecycleOwner(), inventory -> {
            updateInventoryData();
        });
        profileViewModel.getShopItems().observe(getViewLifecycleOwner(), shopItems -> {
            updateInventoryData();

        profileViewModel.getSentFriendRequests().observe(getViewLifecycleOwner(), requests -> {
            updateUI(profileViewModel.getDisplayedUserData().getValue());
        });

       
        binding.buttonLogout.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).performLogout();
            }
        });
        binding.buttonMyFriends.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profile_to_friends);
        });
    }

    private void updateInventoryData() {
        List<UserEquipment> inventory = profileViewModel.getUserInventory().getValue();
        List<EquipmentItem> shopItems = profileViewModel.getShopItems().getValue();

        if (inventory != null && shopItems != null) {
            Map<String, EquipmentItem> definitions = new HashMap<>();
            for (EquipmentItem item : shopItems) {
                definitions.put(item.getId(), item);
            }
            inventoryAdapter.setData(inventory, definitions);
        }
    }

    private void updateUI(User user) {
        if (user == null) return;
        if (user.getAvatarId() != null) {
            binding.imageViewAvatar.setImageResource(AvatarHelper.getAvatarResourceId(user.getAvatarId()));
        }
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        boolean isMyProfile = user.getId().equals(currentUid);

        // --- Prikaz javnih podataka (vidljivo svima) ---
        binding.textViewUsername.setText(user.getUsername());
        binding.textViewTitle.setText(user.getTitle());
        binding.textViewLevel.setText(getString(R.string.level_text, user.getLevel()));
        binding.textViewXp.setText(getString(R.string.xp_text, user.getXp()));
        binding.textViewPp.setText(getString(R.string.pp_text, user.getTotalPp()));
        binding.textViewCoins.setText(getString(R.string.coins_text, user.getCoins()));

        binding.textViewBadges.setText(getString(R.string.profile_badges) + " 0");


        binding.imageViewQrCode.setVisibility(View.VISIBLE);
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
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

        if (isMyProfile) {
            // --- SLUČAJ 1: GLEDAM SVOJ PROFIL ---

            // Prikazuju se moji privatni podaci i kontrole
            binding.buttonLogout.setVisibility(View.VISIBLE);
            binding.buttonMyFriends.setVisibility(View.VISIBLE);
            binding.buttonAddFriend.setVisibility(View.GONE); // Sakrij "Add Friend"
            binding.textViewPp.setVisibility(View.VISIBLE);
            binding.textViewCoins.setVisibility(View.VISIBLE);
            binding.textViewEquipmentTitle.setVisibility(View.VISIBLE);
            binding.recyclerViewInventory.setVisibility(View.VISIBLE);

            binding.textViewPp.setText(getString(R.string.pp_text, user.getPp()));
            binding.textViewCoins.setText(getString(R.string.coins_text, user.getCoins()));

        } else {
            // --- SLUČAJ 2: GLEDAM TUĐI PROFIL ---

            // Sakrivaju se moji privatni podaci i kontrole
            binding.buttonLogout.setVisibility(View.GONE);
            binding.buttonMyFriends.setVisibility(View.GONE);
            binding.buttonAddFriend.setVisibility(View.VISIBLE); // Prikaži "Add Friend"
            binding.textViewPp.setVisibility(View.GONE);
            binding.textViewCoins.setVisibility(View.GONE);

            // Logika za "Add Friend" dugme (identična kao u pretrazi)
            List<Friend> friends = profileViewModel.getFriendsList().getValue();
            List<FriendRequest> sentRequests = profileViewModel.getSentFriendRequests().getValue();

            boolean isAlreadyFriend = friends != null && friends.stream().anyMatch(f -> f.getUserId().equals(user.getId()));
            boolean isRequestSent = sentRequests != null && sentRequests.stream().anyMatch(r -> r.getReceiverId().equals(user.getId()));

            if (isAlreadyFriend) {
                binding.buttonAddFriend.setText("Friends");
                binding.buttonAddFriend.setEnabled(false);
            } else if (isRequestSent) {
                binding.buttonAddFriend.setText("Request Sent");
                binding.buttonAddFriend.setEnabled(false);
            } else {
                binding.buttonAddFriend.setText("Add Friend");
                binding.buttonAddFriend.setEnabled(true);
                binding.buttonAddFriend.setOnClickListener(v -> {
                    profileViewModel.sendFriendRequest(user.getId());
                    // Odmah onemogući dugme za instant feedback
                    binding.buttonAddFriend.setText("Request Sent");
                    binding.buttonAddFriend.setEnabled(false);
                });
            }
        }

        // Logika za opremu ostaje ista, jer se oslanja na 'isMyProfile'
        List<UserEquipment> inventory = profileViewModel.getUserInventory().getValue();
        List<EquipmentItem> shopItems = profileViewModel.getShopItems().getValue();

        if (inventory != null && shopItems != null) {
            Map<String, EquipmentItem> definitions = new HashMap<>();
            for (EquipmentItem item : shopItems) {
                definitions.put(item.getId(), item);
            }

            if (isMyProfile) {
                binding.textViewEquipmentTitle.setText("My Equipment");
                inventoryAdapter.setData(inventory, definitions);
            } else {
                binding.textViewEquipmentTitle.setText("Active Equipment");
                List<UserEquipment> activeInventory = inventory.stream()
                        .filter(UserEquipment::isActive)
                        .collect(Collectors.toList());
                inventoryAdapter.setData(activeInventory, definitions);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
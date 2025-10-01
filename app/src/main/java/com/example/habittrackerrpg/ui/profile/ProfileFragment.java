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
import com.example.habittrackerrpg.data.model.EquipmentItem; // NOVO
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment; // NOVO
import com.example.habittrackerrpg.databinding.FragmentProfileBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap; // NOVO
import java.util.List;   // NOVO
import java.util.Map;    // NOVO

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private InventoryAdapter inventoryAdapter; // NOVO: Adapter za listu opreme

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

        setupRecyclerView(); // NOVO: Pozivamo metodu za podešavanje liste
        setupObservers();    // NOVO: Preimenovao sam metodu da bude jasnije
    }

    // NOVO: Metoda za podešavanje RecyclerView-a
    private void setupRecyclerView() {
        inventoryAdapter = new InventoryAdapter();
        // Koristimo ID koji smo definisali u XML-u
        binding.recyclerViewInventory.setAdapter(inventoryAdapter);
        inventoryAdapter.setOnItemClickListener(userEquipment -> {
            // Kreiramo bundle da pošaljemo ID predmeta
            Bundle bundle = new Bundle();
            bundle.putString("userEquipmentId", userEquipment.getId());

            // Pokrećemo navigaciju ka detaljima
            Navigation.findNavController(requireView()).navigate(R.id.action_profile_to_equipmentDetail, bundle);
        });
    }

    // NOVO: Metoda za postavljanje svih observera
    private void setupObservers() {
        profileViewModel.getUser().observe(getViewLifecycleOwner(), this::updateUI);

        // NOVO: Observeri za inventar
        profileViewModel.getUserInventory().observe(getViewLifecycleOwner(), inventory -> updateInventoryData());
        profileViewModel.getShopItems().observe(getViewLifecycleOwner(), shopItems -> updateInventoryData());

        // Vraćamo originalnu funkcionalnost dugmetu za odjavu
        binding.buttonLogout.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).performLogout();
            }
        });
    }

    // NOVO: Pomoćna metoda koja osvežava podatke u adapteru
    private void updateInventoryData() {
        List<UserEquipment> inventory = profileViewModel.getUserInventory().getValue();
        List<EquipmentItem> shopItems = profileViewModel.getShopItems().getValue();

        if (inventory != null && shopItems != null) {
            // Kreiramo mapu definicija da bi adapter znao imena i opise predmeta
            Map<String, EquipmentItem> definitions = new HashMap<>();
            for (EquipmentItem item : shopItems) {
                definitions.put(item.getId(), item);
            }
            inventoryAdapter.setData(inventory, definitions);
        }
    }

    private void updateUI(User user) {
        if (user == null) return;

        binding.textViewUsername.setText(user.getUsername());
        binding.textViewTitle.setText(user.getTitle());
        binding.textViewLevel.setText(getString(R.string.level_text, user.getLevel()));
        binding.textViewXp.setText(getString(R.string.xp_text, user.getXp()));
        binding.textViewPp.setText(getString(R.string.pp_text, user.getPp()));
        binding.textViewCoins.setText(getString(R.string.coins_text, user.getCoins()));

        // QR kod se generiše samo ako imamo korisničko ime
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
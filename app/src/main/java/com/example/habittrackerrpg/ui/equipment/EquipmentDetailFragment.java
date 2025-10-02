package com.example.habittrackerrpg.ui.equipment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittrackerrpg.data.model.Clothing;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.EquipmentType;
import com.example.habittrackerrpg.data.model.Potion;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.example.habittrackerrpg.data.model.Weapon;
import com.example.habittrackerrpg.databinding.FragmentEquipmentDetailBinding;

import java.util.Optional;

public class EquipmentDetailFragment extends Fragment {

    private FragmentEquipmentDetailBinding binding;
    private EquipmentViewModel viewModel;
    private String userEquipmentId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userEquipmentId = getArguments().getString("userEquipmentId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEquipmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(EquipmentViewModel.class);

        setupObservers();
    }

    private void setupObservers() {
        viewModel.getUserInventory().observe(getViewLifecycleOwner(), inventory -> updateUi());
        viewModel.getShopItems().observe(getViewLifecycleOwner(), shopItems -> updateUi());
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateUi() {
        if (userEquipmentId == null || viewModel.getUserInventory().getValue() == null || viewModel.getShopItems().getValue() == null) {
            return;
        }

        // 1. Pronađi predmet u korisnikovom inventaru po ID-ju
        Optional<UserEquipment> userItemOpt = viewModel.getUserInventory().getValue().stream()
                .filter(item -> userEquipmentId.equals(item.getId()))
                .findFirst();

        if (!userItemOpt.isPresent()) {
            // Ako predmet iz nekog razloga više ne postoji u inventaru, ne radi ništa
            return;
        }
        UserEquipment userItem = userItemOpt.get();

        // 2. Pokušaj da pronađeš definiciju predmeta u listi iz prodavnice
        Optional<EquipmentItem> definitionOpt = viewModel.getShopItems().getValue().stream()
                .filter(item -> userItem.getEquipmentId().equals(item.getId()))
                .findFirst();

        if (definitionOpt.isPresent()) {
            // SLUČAJ 1: Predmet je pronađen u prodavnici (Napitak ili Odeća)
            EquipmentItem definition = definitionOpt.get();
            binding.textViewItemName.setText(definition.getName());
            binding.textViewItemDescription.setText(definition.getDescription());

            if (userItem.getType() == EquipmentType.POTION) {
                Potion potion = (Potion) definition;
                binding.textViewItemEffect.setText(String.format("Effect: +%d%% PP", potion.getPpBoostPercent()));
                binding.textViewItemState.setText(potion.isPermanent() ? "Type: Permanent Potion" : "Type: Single-Use Potion");
                binding.buttonActivate.setVisibility(View.VISIBLE);
                binding.buttonUpgrade.setVisibility(View.GONE);
            } else if (userItem.getType() == EquipmentType.CLOTHING) {
                Clothing clothing = (Clothing) definition;
                binding.textViewItemEffect.setText(String.format("Effect: +%d%% %s bonus", clothing.getEffectValue(), clothing.getClothingType().name()));
                String state = userItem.isActive()
                        ? "State: Active (" + userItem.getBattlesRemaining() + " battles left)"
                        : "State: In Inventory";
                binding.textViewItemState.setText(state);
                binding.buttonActivate.setVisibility(View.VISIBLE);
                binding.buttonUpgrade.setVisibility(View.GONE);
            }

        } else if (userItem.getType() == EquipmentType.WEAPON) {
            // SLUČAJ 2: Definicija nije u prodavnici, pretpostavljamo da je Oružje
            binding.textViewItemName.setText("Weapon"); // Privremeni naziv
            binding.textViewItemDescription.setText("This powerful item is won in battle."); // Privremeni opis
            binding.textViewItemEffect.setText(String.format("Current Bonus: +%.2f%%", userItem.getCurrentUpgradeBonus()));
            binding.textViewItemState.setText("State: Always Active");
            binding.buttonActivate.setVisibility(View.GONE);
            binding.buttonUpgrade.setVisibility(View.VISIBLE);
        }

        EquipmentItem definition = definitionOpt.get();

        if (definition.getIcon() != null && !definition.getIcon().isEmpty() && getContext() != null) {
            int iconId = getContext().getResources().getIdentifier(
                    definition.getIcon(), "drawable", getContext().getPackageName());
            if (iconId != 0) {
                binding.imageViewItemIcon.setImageResource(iconId);
            }
        }

        if (userItem.isActive()) {
            binding.buttonActivate.setEnabled(false);
            binding.buttonActivate.setText("Activated");
        } else {
            binding.buttonActivate.setEnabled(true);
            binding.buttonActivate.setText("Activate");
        }

        binding.buttonActivate.setOnClickListener(v -> {
            viewModel.activateItem(userItem);
        });

        binding.buttonUpgrade.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Upgrade logic not implemented yet.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
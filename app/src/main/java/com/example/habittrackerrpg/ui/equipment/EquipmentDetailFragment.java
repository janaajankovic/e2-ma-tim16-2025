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
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> updateUi());
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
        if (userEquipmentId == null || viewModel.getUserInventory().getValue() == null ||
                viewModel.getShopItems().getValue() == null || viewModel.getCurrentUser().getValue() == null) {
            return;
        }
        Optional<UserEquipment> userItemOpt = viewModel.getUserInventory().getValue().stream()
                .filter(item -> userEquipmentId.equals(item.getId()))
                .findFirst();

        if (!userItemOpt.isPresent()) {
            return;
        }
        UserEquipment userItem = userItemOpt.get();

        Optional<EquipmentItem> definitionOpt = viewModel.getShopItems().getValue().stream()
                .filter(def -> userItem.getEquipmentId().equals(def.getId()))
                .findFirst();

        if (definitionOpt.isPresent()) {
            EquipmentItem definition = definitionOpt.get();

            binding.textViewItemName.setText(definition.getName());
            binding.textViewItemDescription.setText(definition.getDescription());
            if (definition.getIcon() != null && !definition.getIcon().isEmpty() && getContext() != null) {
                int iconId = getContext().getResources().getIdentifier(definition.getIcon(), "drawable", getContext().getPackageName());
                if (iconId != 0) {
                    binding.imageViewItemIcon.setImageResource(iconId);
                }
            }

            if (userItem.getType() == EquipmentType.WEAPON) {
                Weapon weaponDef = (Weapon) definition;
                double totalBonus = weaponDef.getEffectValue() + (userItem.getCurrentUpgradeBonus() * 100);

                switch(weaponDef.getWeaponType()) {
                    case SWORD:
                        binding.textViewItemEffect.setText(String.format("Current bonus: +%.2f%% power", totalBonus));
                        break;
                    case BOW_AND_ARROW:
                        binding.textViewItemEffect.setText(String.format("Current bonus: +%.2f%% more coins", totalBonus));
                        break;
                }

                binding.textViewItemState.setText("State: Always active");
                binding.buttonActivate.setVisibility(View.GONE);
                binding.buttonUpgrade.setVisibility(View.VISIBLE);

                long upgradeCost = viewModel.getWeaponUpgradeCost();
                binding.buttonUpgrade.setText("Upgrade (" + upgradeCost + " coins)");

                binding.buttonUpgrade.setOnClickListener(v -> viewModel.upgradeWeapon(userItem));

            } else {
                binding.buttonActivate.setVisibility(View.VISIBLE);
                binding.buttonUpgrade.setVisibility(View.GONE);

                if(userItem.getType() == EquipmentType.POTION) {
                    Potion potion = (Potion) definition;
                    binding.textViewItemEffect.setText(String.format("Effect: +%d%% PP", potion.getPpBoostPercent()));
                    binding.textViewItemState.setText(potion.isPermanent() ? "Type: Permanent Potion" : "Type: Single-Use Potion");
                } else if (userItem.getType() == EquipmentType.CLOTHING) {
                    Clothing clothing = (Clothing) definition;
                    binding.textViewItemEffect.setText(String.format("Effect: +%d%% %s bonus", clothing.getEffectValue(), clothing.getClothingType().name().toLowerCase()));
                    String state = userItem.isActive()
                            ? "State: Active (" + userItem.getBattlesRemaining() + " battles remaining)"
                            : "State: In Inventory";
                    binding.textViewItemState.setText(state);
                }

                if (userItem.isActive()) {
                    binding.buttonActivate.setEnabled(false);
                    binding.buttonActivate.setText("Activated");
                } else {
                    binding.buttonActivate.setEnabled(true);
                    binding.buttonActivate.setText("Activate");
                }
                binding.buttonActivate.setOnClickListener(v -> viewModel.activateItem(userItem));
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
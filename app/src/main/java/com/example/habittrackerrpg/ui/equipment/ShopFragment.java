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
import com.example.habittrackerrpg.data.model.Potion;
import com.example.habittrackerrpg.databinding.FragmentShopBinding;

public class ShopFragment extends Fragment {

    private FragmentShopBinding binding;
    private EquipmentViewModel viewModel;
    private ShopAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EquipmentViewModel.class);

        setupRecyclerView();
        setupObservers();

        //addTestDataToShop();
    }

    private void setupRecyclerView() {
        adapter = new ShopAdapter();
        binding.recyclerViewShop.setAdapter(adapter);

        adapter.setOnBuyButtonClickListener(item -> {
            viewModel.buyItem(item);
        });
    }

    private void setupObservers() {
        viewModel.getShopItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.setItems(items);
            }
        });

        viewModel.getCalculatedPrices().observe(getViewLifecycleOwner(), prices -> {
            if (prices != null) {
                adapter.setPrices(prices);
            }
        });

        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.textViewCoinBalance.setText(String.format("Coins: %d", user.getCoins()));
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTestDataToShop() {
        Potion potion1 = new Potion("Minor Power Potion", "Increases PP by 20% for one battle.", 50, false, 20, "ic_potion1");
        Potion potion2 = new Potion("Major Power Potion", "Increases PP by 40% for one battle.", 70, false, 40, "ic_potion2");
        Potion potion3 = new Potion("Elixir of Strength", "Permanently increases PP by 5%.", 200, true, 5, "ic_potion3");
        Potion potion4 = new Potion("Greater Elixir of Strength", "Permanently increases PP by 10%.", 1000, true, 10, "ic_potion4");

        Clothing gloves = new Clothing("Power Gloves", "Increases PP by 10%. Lasts 2 battles.", 60, Clothing.ClothingType.GLOVES, 10, "ic_gloves");
        Clothing shield = new Clothing("Guardian Shield", "Increases attack chance by 10%. Lasts 2 battles.", 60, Clothing.ClothingType.SHIELD, 10, "ic_shield2");
        Clothing boots = new Clothing("Boots of Haste", "40% chance for an extra attack. Lasts 2 battles.", 80, Clothing.ClothingType.BOOTS, 40, "ic_boots");

        viewModel.addShopItemForTesting(potion1);
        viewModel.addShopItemForTesting(potion2);
        viewModel.addShopItemForTesting(potion3);
        viewModel.addShopItemForTesting(potion4);
        viewModel.addShopItemForTesting(gloves);
        viewModel.addShopItemForTesting(shield);
        viewModel.addShopItemForTesting(boots);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
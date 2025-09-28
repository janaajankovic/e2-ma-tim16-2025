package com.example.habittrackerrpg.ui.categories;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Category;
import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class ManageCategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryActionsListener {

    private CategoryViewModel categoryViewModel;
    private CategoryAdapter adapter;
    private String selectedColor = "#FFC107";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoryAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                adapter.setCategories(categories);
            }
        });

        categoryViewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add_category);
        fab.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Category");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        final TextInputEditText categoryNameInput = dialogView.findViewById(R.id.edit_text_category_name);
        final View selectedColorView = dialogView.findViewById(R.id.view_selected_color);
        final Button changeColorButton = dialogView.findViewById(R.id.button_change_color);

        selectedColorView.getBackground().mutate().setTint(Color.parseColor(selectedColor));

        changeColorButton.setOnClickListener(v -> {
            new ColorPickerDialog
                    .Builder(requireContext())
                    .setTitle("Choose Color")
                    .setColorListener((color, colorHex) -> {
                        selectedColor = colorHex;
                        selectedColorView.getBackground().mutate().setTint(color);
                    })
                    .setPositiveButton("Confirm")
                    .setNegativeButton("Cancel")
                    .show();
        });

        builder.setPositiveButton("Add", (dialog, which) -> {
            String categoryName = categoryNameInput.getText().toString();
            categoryViewModel.addCategory(categoryName, selectedColor);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onEditClick(Category category) {
        new ColorPickerDialog
                .Builder(requireContext())
                .setTitle("Change Color")
                .setDefaultColor(category.getColor())
                .setColorListener((color, colorHex) -> {
                    category.setColor(colorHex);
                    categoryViewModel.updateCategory(category);
                })
                .setPositiveButton("Confirm")
                .setNegativeButton("Cancel")
                .show();
    }

    @Override
    public void onDeleteClick(Category category) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + category.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    categoryViewModel.deleteCategory(category);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
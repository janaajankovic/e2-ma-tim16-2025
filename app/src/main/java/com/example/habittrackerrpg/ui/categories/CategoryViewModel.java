package com.example.habittrackerrpg.ui.categories;

import android.graphics.Color;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.repository.CategoryRepository;

import java.util.List;

public class CategoryViewModel extends ViewModel {

    private CategoryRepository categoryRepository;
    private LiveData<List<Category>> categoriesLiveData;

    private MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public CategoryViewModel() {
        categoryRepository = new CategoryRepository();
        categoriesLiveData = categoryRepository.getCategories();
    }

    public LiveData<List<Category>> getCategories() {
        return categoriesLiveData;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void addCategory(String name, String color) {
        // Validation logic is now here, in the ViewModel
        if (name == null || name.trim().isEmpty()) {
            toastMessage.setValue("Category name cannot be empty.");
            return;
        }

        List<Category> currentCategories = categoriesLiveData.getValue();
        if (currentCategories != null) {
            for (Category category : currentCategories) {
                if (Color.parseColor(category.getColor()) == Color.parseColor(color)) {
                    toastMessage.setValue("This color is already taken!");
                    return;
                }
            }
        }

        Category newCategory = new Category(name.trim(), color);
        categoryRepository.addCategory(newCategory);
        toastMessage.setValue("Category saved.");
    }

    public void updateCategory(Category categoryToUpdate) {
        List<Category> currentCategories = categoriesLiveData.getValue();
        if (currentCategories != null) {
            for (Category existingCategory : currentCategories) {
                if (existingCategory.getColor().equalsIgnoreCase(categoryToUpdate.getColor())
                        && !existingCategory.getId().equals(categoryToUpdate.getId())) {
                    toastMessage.setValue("This color is already in use by another category!");
                    return;
                }
            }
        }

        categoryRepository.updateCategory(categoryToUpdate);
        toastMessage.setValue("Category updated.");
    }

    public void deleteCategory(Category category) {
        categoryRepository.deleteCategory(category);
    }
}
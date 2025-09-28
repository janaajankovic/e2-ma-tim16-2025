package com.example.habittrackerrpg.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habittrackerrpg.data.model.Category;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {
    private static final String TAG = "CategoryRepository";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();

    public LiveData<List<Category>> getCategories() {
        if (mAuth.getCurrentUser() == null) {
            return categoriesLiveData;
        }
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("categories")
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (snapshots != null) {
                        // Manually map documents to capture the ID
                        List<Category> categoryList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Category category = doc.toObject(Category.class);
                            if (category != null) {
                                category.setId(doc.getId());
                                categoryList.add(category);
                            }
                        }
                        categoriesLiveData.setValue(categoryList);
                    }
                });
        return categoriesLiveData;
    }

    // Adds a new category for the current user
    public void addCategory(Category category) {
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, cannot add category.");
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("categories")
                .add(category)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Category added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding category", e));
    }

    public void updateCategory(Category category) {
        if (mAuth.getCurrentUser() == null || category.getId() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("categories").document(category.getId())
                .set(category) // .set() will update the whole object
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Category updated successfully"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating category", e));
    }

    public void deleteCategory(Category category) {
        if (mAuth.getCurrentUser() == null || category.getId() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("categories").document(category.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Category deleted successfully"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting category", e));
    }
}
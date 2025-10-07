package com.example.habittrackerrpg.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.habittrackerrpg.data.model.Clothing;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.EquipmentType;
import com.example.habittrackerrpg.data.model.Potion;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class EquipmentRepository {

    private static final String TAG = "EquipmentRepository";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<EquipmentItem>> getShopItems() {
        MutableLiveData<List<EquipmentItem>> shopItemsLiveData = new MutableLiveData<>();

        db.collection("shop_equipment").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<EquipmentItem> items = new ArrayList<>();
                task.getResult().forEach(doc -> {
                    try {
                        String typeString = doc.getString("type");
                        if (typeString == null) return;

                        EquipmentType type = EquipmentType.valueOf(typeString);
                        EquipmentItem item = null;

                        if (type == EquipmentType.POTION) {
                            item = doc.toObject(Potion.class);
                        } else if (type == EquipmentType.CLOTHING) {
                            item = doc.toObject(Clothing.class);
                        }

                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Invalid equipment type in database: " + doc.getString("type"));
                    }
                });
                shopItemsLiveData.setValue(items);
            } else {
                Log.w(TAG, "Error getting shop items.", task.getException());
            }
        });

        return shopItemsLiveData;
    }

    public LiveData<List<UserEquipment>> getUserInventory(String userId) {
        MutableLiveData<List<UserEquipment>> inventoryLiveData = new MutableLiveData<>();
        if (userId == null) {
            inventoryLiveData.setValue(new ArrayList<>());
            return inventoryLiveData;
        }

        db.collection("users").document(userId).collection("inventory")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Inventory listen failed.", e);
                        return;
                    }
                    if (snapshots != null) {
                        List<UserEquipment> inventory = new ArrayList<>();
                        snapshots.forEach(doc -> {
                            UserEquipment userEquipment = doc.toObject(UserEquipment.class);
                            userEquipment.setId(doc.getId());
                            inventory.add(userEquipment);
                        });
                        inventoryLiveData.setValue(inventory);
                    }
                });

        return inventoryLiveData;
    }

    public void buyItem(User currentUserData, EquipmentItem itemToBuy, long calculatedPrice, BuyItemCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onResult(false, "User not logged in.");
            return;
        }
        String uid = firebaseUser.getUid();

        DocumentReference userRef = db.collection("users").document(uid);
        DocumentReference newInventoryItemRef = userRef.collection("inventory").document();

        db.runTransaction(transaction -> {
            if (currentUserData.getCoins() < calculatedPrice) {
                throw new IllegalStateException("Not enough coins.");
            }

            long newCoinBalance = currentUserData.getCoins() - calculatedPrice;
            transaction.update(userRef, "coins", newCoinBalance);

            UserEquipment newInventoryItem = new UserEquipment(uid, itemToBuy.getId(), itemToBuy.getType());
            transaction.set(newInventoryItemRef, newInventoryItem);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Purchase successful!");
            callback.onResult(true, "Item purchased successfully!");
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Purchase failed", e);
            callback.onResult(false, e.getMessage());
        });
    }

    public interface BuyItemCallback {
        void onResult(boolean success, String message);
    }

    public void addShopItemForTesting(EquipmentItem item) {
        db.collection("shop_equipment").add(item)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Shop item added: " + docRef.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to add shop item", e));
    }

    public void updateUserEquipment(UserEquipment itemToUpdate) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null || itemToUpdate.getId() == null) {
            Log.e(TAG, "Cannot update item. User not logged in or item has no ID.");
            return;
        }
        String uid = firebaseUser.getUid();

        db.collection("users").document(uid).collection("inventory").document(itemToUpdate.getId())
                .set(itemToUpdate)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "UserEquipment item successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating UserEquipment item", e));
    }

    public LiveData<List<UserEquipment>> getActiveUserInventory(String userId) {
        MutableLiveData<List<UserEquipment>> inventoryLiveData = new MutableLiveData<>();
        if (userId == null) {
            inventoryLiveData.setValue(new ArrayList<>());
            return inventoryLiveData;
        }

        db.collection("users").document(userId).collection("inventory")
                .whereEqualTo("active", true)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Active inventory listen failed.", e);
                        return;
                    }
                    if (snapshots != null) {
                        List<UserEquipment> inventory = new ArrayList<>();
                        snapshots.forEach(doc -> {
                            UserEquipment userEquipment = doc.toObject(UserEquipment.class);
                            userEquipment.setId(doc.getId());
                            inventory.add(userEquipment);
                        });
                        inventoryLiveData.setValue(inventory);
                    }
                });

        return inventoryLiveData;
  }
  
  public void deleteUserEquipment(String userEquipmentId) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null || userEquipmentId == null) {
            Log.e(TAG, "Cannot delete item. User not logged in or item has no ID.");
            return;
        }
        String uid = firebaseUser.getUid();

        db.collection("users").document(uid).collection("inventory").document(userEquipmentId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "UserEquipment item successfully deleted!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting UserEquipment item", e));
    }
}
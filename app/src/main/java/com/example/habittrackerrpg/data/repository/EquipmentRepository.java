package com.example.habittrackerrpg.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.habittrackerrpg.data.model.Clothing;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.EquipmentType;
import com.example.habittrackerrpg.data.model.Potion;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.example.habittrackerrpg.data.model.Weapon;
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

    private final AllianceRepository allianceRepository;

    public EquipmentRepository(Context context) {
        this.allianceRepository = new AllianceRepository(context);
    }

    // --- INTERFEJSI ZA CALLBACK ---
    public interface BuyItemCallback {
        void onResult(boolean success, String message);
    }
    public interface CompletionCallback {
        void onComplete(boolean success);
    }

    // --- METODE ---

    public LiveData<List<EquipmentItem>> getShopItems() {
        MutableLiveData<List<EquipmentItem>> shopItemsLiveData = new MutableLiveData<>();

        db.collection("shop_equipment").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Error getting shop items.", e);
                return;
            }
            if (snapshots != null) {
                List<EquipmentItem> items = new ArrayList<>();
                snapshots.forEach(doc -> {
                    try {
                        String typeString = doc.getString("type");
                        if (typeString == null) return;
                        EquipmentType type = EquipmentType.valueOf(typeString);
                        EquipmentItem item = null;

                        if (type == EquipmentType.POTION) {
                            item = doc.toObject(Potion.class);
                        } else if (type == EquipmentType.CLOTHING) {
                            item = doc.toObject(Clothing.class);
                        } else if (type == EquipmentType.WEAPON) {
                            item = doc.toObject(Weapon.class);
                        }

                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Error parsing shop item", ex);
                    }
                });
                shopItemsLiveData.setValue(items);
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
            User freshUser = transaction.get(userRef).toObject(User.class);
            if (freshUser.getCoins() < calculatedPrice) {
                throw new IllegalStateException("Not enough coins.");
            }
            long newCoinBalance = freshUser.getCoins() - calculatedPrice;
            transaction.update(userRef, "coins", newCoinBalance);
            UserEquipment newInventoryItem = new UserEquipment(uid, itemToBuy.getId(), itemToBuy.getType());
            transaction.set(newInventoryItemRef, newInventoryItem);
            return null;
        }).addOnSuccessListener(aVoid -> {
            callback.onResult(true, "Item purchased successfully!");
            allianceRepository.logMissionAction("SHOP_PURCHASE", 2);
        }).addOnFailureListener(e -> {
            callback.onResult(false, e.getMessage());
        });
    }

    public void updateUserEquipment(UserEquipment itemToUpdate, CompletionCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null || itemToUpdate.getId() == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }
        String uid = firebaseUser.getUid();
        db.collection("users").document(uid).collection("inventory").document(itemToUpdate.getId())
                .set(itemToUpdate)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onComplete(false);
                });
    }

    public void deleteUserEquipment(String userEquipmentId, CompletionCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null || userEquipmentId == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }
        String uid = firebaseUser.getUid();
        db.collection("users").document(uid).collection("inventory").document(userEquipmentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onComplete(false);
                });
    }

    public void addUserEquipment(UserEquipment item) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;
        String uid = firebaseUser.getUid();
        db.collection("users").document(uid).collection("inventory").document().set(item);
    }

    public void addShopItemForTesting(EquipmentItem item) {
        db.collection("shop_equipment").add(item);
    }
}
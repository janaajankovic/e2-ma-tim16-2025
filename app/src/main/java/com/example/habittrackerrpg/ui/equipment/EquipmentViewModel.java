package com.example.habittrackerrpg.ui.equipment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.example.habittrackerrpg.data.repository.EquipmentRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.logic.Event;

import java.util.List;

public class EquipmentViewModel extends ViewModel {

    private final EquipmentRepository equipmentRepository;
    private final ProfileRepository profileRepository;
    private final LiveData<List<EquipmentItem>> shopItems;
    private final LiveData<List<UserEquipment>> userInventory;
    private final LiveData<User> currentUser;
    private final MutableLiveData<Event<String>> toastMessage = new MutableLiveData<>();

    public EquipmentViewModel() {
        this.equipmentRepository = new EquipmentRepository();
        this.profileRepository = new ProfileRepository();

        this.shopItems = equipmentRepository.getShopItems();
        this.userInventory = equipmentRepository.getUserInventory();
        this.currentUser = profileRepository.getUserLiveData();
    }

    public LiveData<List<EquipmentItem>> getShopItems() {
        return shopItems;
    }
    public LiveData<List<UserEquipment>> getUserInventory() {
        return userInventory;
    }
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
    public LiveData<Event<String>> getToastMessage() {
        return toastMessage;
    }
    public void buyItem(EquipmentItem itemToBuy) {
        User user = currentUser.getValue();
        if (user == null) {
            toastMessage.setValue(new Event<>("Error: User not loaded."));
            return;
        }

        if (user.getCoins() < itemToBuy.getCost()) {
            toastMessage.setValue(new Event<>("Not enough coins!"));
            return;
        }

        equipmentRepository.buyItem(user, itemToBuy, (success, message) -> {
            toastMessage.setValue(new Event<>(message));
        });
    }
    public void addShopItemForTesting(EquipmentItem item) {
        equipmentRepository.addShopItemForTesting(item);
    }
}
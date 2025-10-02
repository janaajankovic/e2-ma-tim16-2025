package com.example.habittrackerrpg.ui.equipment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.Clothing;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.EquipmentType;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.example.habittrackerrpg.data.repository.EquipmentRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.logic.Event;
import com.google.firebase.auth.FirebaseAuth;

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
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        this.shopItems = equipmentRepository.getShopItems();
        this.userInventory = equipmentRepository.getUserInventory(currentUid);
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
    public void activateItem(UserEquipment itemToActivate) {
        if (itemToActivate.isActive()) {
            toastMessage.setValue(new Event<>("Item is already active."));
            return;
        }

        if (itemToActivate.getType() == EquipmentType.CLOTHING) {
            List<UserEquipment> currentInventory = userInventory.getValue();
            if (currentInventory != null) {
                EquipmentItem definition = getShopItems().getValue().stream()
                        .filter(def -> def.getId().equals(itemToActivate.getEquipmentId()))
                        .findFirst().orElse(null);

                if (definition instanceof Clothing) {
                    Clothing.ClothingType typeToActivate = ((Clothing) definition).getClothingType();

                    for (UserEquipment itemInInventory : currentInventory) {
                        if (itemInInventory.isActive() && itemInInventory.getType() == EquipmentType.CLOTHING) {
                            EquipmentItem invDef = getShopItems().getValue().stream()
                                    .filter(def -> def.getId().equals(itemInInventory.getEquipmentId()))
                                    .findFirst().orElse(null);

                            if (invDef instanceof Clothing && ((Clothing) invDef).getClothingType() == typeToActivate) {
                                toastMessage.setValue(new Event<>("You can only have one " + typeToActivate.name().toLowerCase() + " item active at a time."));
                                return;
                            }
                        }
                    }
                }
            }
        }

        itemToActivate.setActive(true);
        equipmentRepository.updateUserEquipment(itemToActivate);
        toastMessage.setValue(new Event<>("Item activated!"));
    }
}
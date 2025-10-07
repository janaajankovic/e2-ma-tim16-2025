package com.example.habittrackerrpg.ui.equipment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.Clothing;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.EquipmentType;
import com.example.habittrackerrpg.data.model.Potion;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.example.habittrackerrpg.data.repository.EquipmentRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.logic.Event;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EquipmentViewModel extends ViewModel {

    private final EquipmentRepository equipmentRepository;
    private final ProfileRepository profileRepository;
    private final LiveData<List<EquipmentItem>> shopItems;
    private final LiveData<List<UserEquipment>> userInventory;
    private final LiveData<User> currentUser;
    private final MutableLiveData<Event<String>> toastMessage = new MutableLiveData<>();
    private final MediatorLiveData<Map<String, Long>> calculatedPrices = new MediatorLiveData<>();
    public EquipmentViewModel() {
        this.equipmentRepository = new EquipmentRepository();
        this.profileRepository = new ProfileRepository();
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        this.shopItems = equipmentRepository.getShopItems();
        this.userInventory = equipmentRepository.getUserInventory(currentUid);
        this.currentUser = profileRepository.getUserLiveData();

        currentUser.observeForever(user -> recalculateAndSaveStats());
        userInventory.observeForever(inventory -> recalculateAndSaveStats());
        shopItems.observeForever(items -> recalculateAndSaveStats());

        calculatedPrices.addSource(currentUser, user -> calculatePrices());
        calculatedPrices.addSource(shopItems, items -> calculatePrices());
    }

    private void recalculateAndSaveStats() {
        User user = currentUser.getValue();
        List<UserEquipment> inventory = userInventory.getValue();
        List<EquipmentItem> definitions = shopItems.getValue();
        if (user == null || inventory == null || definitions == null) return;

        Map<String, EquipmentItem> defMap = new HashMap<>();
        for (EquipmentItem item : definitions) {
            defMap.put(item.getId(), item);
        }

        double calculatedPp = user.getPp();
        calculatedPp *= (1 + user.getPermanentPpBonusPercent());
        double temporaryPpBonusPercent = 0;

        double attackChanceBonus = 0;
        int extraAttacks = 0;

        for (UserEquipment userItem : inventory) {
            if (userItem.isActive()) {
                EquipmentItem def = defMap.get(userItem.getEquipmentId());
                if (def instanceof Potion) {
                    temporaryPpBonusPercent += ((Potion) def).getPpBoostPercent();
                } else if (def instanceof Clothing) {
                    Clothing clothing = (Clothing) def;
                    switch (clothing.getClothingType()) {
                        case GLOVES:
                            temporaryPpBonusPercent += clothing.getEffectValue();
                            break;
                        case SHIELD:
                            attackChanceBonus += clothing.getEffectValue();
                            break;
                        case BOOTS:
                            if (Math.random() < 0.40) extraAttacks += 1;
                            break;
                    }
                }
            }
        }
        calculatedPp *= (1 + (temporaryPpBonusPercent / 100.0));

        user.setTotalPp((long) calculatedPp);
        user.setTotalAttackChanceBonus(attackChanceBonus / 100.0);
        user.setTotalExtraAttacks(extraAttacks);

        profileRepository.updateUser(user);
    }

    private void calculatePrices() {
        User user = currentUser.getValue();
        List<EquipmentItem> items = shopItems.getValue();
        if (user == null || items == null) return;

        int levelForPriceCalc = (user.getLevel() > 1) ? user.getLevel() - 1 : 1;
        long previousLevelReward = calculateBossRewardForLevel(levelForPriceCalc);

        Map<String, Long> priceMap = new HashMap<>();
        for (EquipmentItem item : items) {
            long price = (long) (previousLevelReward * (item.getCost() / 100.0));
            priceMap.put(item.getId(), price);
        }
        calculatedPrices.setValue(priceMap);
    }

    private long calculateBossRewardForLevel(int level) {
        if (level <= 0) return 200;
        if (level == 1) return 200;
        double previousLevelReward = calculateBossRewardForLevel(level - 1);
        return (long) (previousLevelReward * 1.20);
    }

    public LiveData<Map<String, Long>> getCalculatedPrices() {
        return calculatedPrices;
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

        int levelForPriceCalc = (user.getLevel() > 1) ? user.getLevel() - 1 : 1;
        long previousLevelReward = calculateBossRewardForLevel(levelForPriceCalc);
        long currentPrice = (long) (previousLevelReward * (itemToBuy.getCost() / 100.0));

        if (user.getCoins() < currentPrice) {
            toastMessage.setValue(new Event<>("Not enough coins!"));
            return;
        }

        equipmentRepository.buyItem(user, itemToBuy, currentPrice, (success, message) -> {
            toastMessage.setValue(new Event<>("Success"));
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

        User user = currentUser.getValue();
        List<EquipmentItem> definitions = shopItems.getValue();
        List<UserEquipment> currentInventory = userInventory.getValue();
        if (user == null || definitions == null || currentInventory == null) {
            toastMessage.setValue(new Event<>("Data not ready, please try again."));
            return;
        }

        EquipmentItem definition = definitions.stream()
                .filter(def -> def.getId().equals(itemToActivate.getEquipmentId()))
                .findFirst().orElse(null);

        if (definition == null) {
            toastMessage.setValue(new Event<>("Item definition not found."));
            return;
        }

        if (definition instanceof Potion) {
            Potion potion = (Potion) definition;
            if (potion.isPermanent()) {
                double currentBonus = user.getPermanentPpBonusPercent();
                double newBonus = currentBonus + (potion.getPpBoostPercent() / 100.0);
                user.setPermanentPpBonusPercent(newBonus);

                recalculateAndSaveStats();

                equipmentRepository.deleteUserEquipment(itemToActivate.getId());
                toastMessage.setValue(new Event<>(definition.getName() + " consumed. Permanent bonus applied!"));
                return;
            }
        }

        if (itemToActivate.getType() == EquipmentType.CLOTHING) {
            if (definition instanceof Clothing) {
                Clothing.ClothingType typeToActivate = ((Clothing) definition).getClothingType();
                for (UserEquipment itemInInventory : currentInventory) {
                    if (itemInInventory.isActive() && itemInInventory.getType() == EquipmentType.CLOTHING) {
                        EquipmentItem invDef = definitions.stream()
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

        itemToActivate.setActive(true);
        equipmentRepository.updateUserEquipment(itemToActivate);
        toastMessage.setValue(new Event<>(definition.getName() + " activated!"));
    }
}
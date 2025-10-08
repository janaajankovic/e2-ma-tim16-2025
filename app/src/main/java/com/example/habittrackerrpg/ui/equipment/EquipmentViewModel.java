package com.example.habittrackerrpg.ui.equipment;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.habittrackerrpg.data.model.*;
import com.example.habittrackerrpg.data.repository.EquipmentRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.logic.Event;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentViewModel extends AndroidViewModel {

    private final EquipmentRepository equipmentRepository;
    private final ProfileRepository profileRepository;
    private final LiveData<List<EquipmentItem>> shopItems;
    private final LiveData<List<UserEquipment>> userInventory;
    private final LiveData<User> currentUser;
    private final MutableLiveData<Event<String>> toastMessage = new MutableLiveData<>();
    private final MediatorLiveData<Map<String, Long>> calculatedPrices = new MediatorLiveData<>();

    public EquipmentViewModel(@NonNull Application application) {
        super(application);
        this.equipmentRepository = new EquipmentRepository(application.getApplicationContext());
        this.profileRepository = new ProfileRepository();
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        this.shopItems = equipmentRepository.getShopItems();
        this.userInventory = equipmentRepository.getUserInventory(currentUid);
        this.currentUser = profileRepository.getUserLiveData();

        // Listeneri za dinamičke cene
        calculatedPrices.addSource(currentUser, user -> calculatePrices());
        calculatedPrices.addSource(shopItems, items -> calculatePrices());
    }

    // --- GLAVNE AKCIJE ---

    public void buyItem(EquipmentItem itemToBuy) {
        User user = currentUser.getValue();
        if (user == null) {
            toastMessage.setValue(new Event<>("Error: User not loaded."));
            return;
        }

        // Računamo cenu ponovo u trenutku kupovine, tačno po specifikaciji
        int levelForPriceCalc = (user.getLevel() > 1) ? user.getLevel() - 1 : 1;
        long previousLevelReward = calculateBossRewardForLevel(levelForPriceCalc);
        long currentPrice = (long) (previousLevelReward * (itemToBuy.getCost() / 100.0));

        if (user.getCoins() < currentPrice) {
            toastMessage.setValue(new Event<>("Not enough coins!"));
            return;
        }

        equipmentRepository.buyItem(user, itemToBuy, currentPrice, (success, message) -> {
            if (success) toastMessage.setValue(new Event<>("Item purchased!"));
            else toastMessage.setValue(new Event<>(message));
        });
    }

    public void activateItem(UserEquipment itemToActivate) {
        if (itemToActivate.isActive()) {
            toastMessage.setValue(new Event<>("Item is already active."));
            return;
        }

        User user = currentUser.getValue();
        List<EquipmentItem> definitions = shopItems.getValue();
        List<UserEquipment> currentInventory = userInventory.getValue();
        if (user == null || definitions == null || currentInventory == null) return;

        EquipmentItem definition = definitions.stream()
                .filter(def -> def.getId().equals(itemToActivate.getEquipmentId()))
                .findFirst().orElse(null);
        if (definition == null) return;

        if (definition instanceof Potion) {
            Potion potion = (Potion) definition;
            if (potion.isPermanent()) {
                // 1. Ažuriraj trajni bonus na LOKALNOM user objektu
                double currentBonus = user.getPermanentPpBonusPercent();
                user.setPermanentPpBonusPercent(currentBonus + (potion.getPpBoostPercent() / 100.0));

                // 2. Obriši napitak, a NAKON toga sačuvaj korisnika sa preračunatim statistikama
                equipmentRepository.deleteUserEquipment(itemToActivate.getId(), success -> {
                    if (success) recalculateAndSaveStats();
                });
                toastMessage.setValue(new Event<>(definition.getName() + " consumed. Stats updated!"));
                return;
            }
        }

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

        // Za odeću i privremene napitke, samo postavi 'isActive' i preračunaj
        itemToActivate.setActive(true);
        equipmentRepository.updateUserEquipment(itemToActivate, success -> {
            if (success) recalculateAndSaveStats();
        });
        toastMessage.setValue(new Event<>(definition.getName() + " activated!"));
    }

    public void upgradeWeapon(UserEquipment weaponToUpgrade) {
        User user = currentUser.getValue();
        if (user == null || weaponToUpgrade.getType() != EquipmentType.WEAPON) return;

        int levelForPriceCalc = (user.getLevel() > 1) ? user.getLevel() - 1 : 1;
        long previousLevelReward = calculateBossRewardForLevel(levelForPriceCalc);
        long upgradeCost = (long) (previousLevelReward * 0.60);

        if (user.getCoins() < upgradeCost) {
            toastMessage.setValue(new Event<>("Not enough coins! Cost: " + upgradeCost));
            return;
        }

        user.setCoins(user.getCoins() - upgradeCost);
        weaponToUpgrade.setCurrentUpgradeBonus(weaponToUpgrade.getCurrentUpgradeBonus() + 0.01);

        equipmentRepository.updateUserEquipment(weaponToUpgrade, success -> {
            if (success) recalculateAndSaveStats();
        });
        toastMessage.setValue(new Event<>("Weapon upgraded successfully!"));
    }

    // --- "MOZAK" ZA PRORAČUN STATISTIKA ---
    private void recalculateAndSaveStats() {
        User user = currentUser.getValue();
        List<UserEquipment> inventory = userInventory.getValue();
        List<EquipmentItem> definitions = shopItems.getValue();
        if (user == null || inventory == null || definitions == null) return;

        Map<String, EquipmentItem> defMap = new HashMap<>();
        for (EquipmentItem item : definitions) {
            defMap.put(item.getId(), item);
        }

        double tempPpBonusPercent = 0;
        double attackChanceBonus = 0;
        int extraAttacks = 0;
        double coinBonusPercent = 0.0;

        // Počni proračun od osnovnog 'pp' i trajnog bonusa od napitaka
        double calculatedPp = user.getPp() * (1 + user.getPermanentPpBonusPercent());

        for (UserEquipment userItem : inventory) {
            EquipmentItem def = defMap.get(userItem.getEquipmentId());
            if (def == null) continue;

            if (def instanceof Weapon) {
                Weapon weapon = (Weapon) def;
                double totalWeaponBonus = weapon.getEffectValue() + userItem.getCurrentUpgradeBonus();
                switch (weapon.getWeaponType()) {
                    case SWORD:
                        calculatedPp *= (1 + (totalWeaponBonus / 100.0));
                        break;
                    case BOW_AND_ARROW:
                        coinBonusPercent += totalWeaponBonus;
                        break;
                }
            }

            if (userItem.isActive()) {
                if (def instanceof Potion) {
                    tempPpBonusPercent += ((Potion) def).getPpBoostPercent();
                } else if (def instanceof Clothing) {
                    Clothing clothing = (Clothing) def;
                    switch (clothing.getClothingType()) {
                        case GLOVES: tempPpBonusPercent += clothing.getEffectValue(); break;
                        case SHIELD: attackChanceBonus += clothing.getEffectValue(); break;
                        case BOOTS: if (Math.random() < 0.40) extraAttacks += 1; break;
                    }
                }
            }
        }
        calculatedPp *= (1 + (tempPpBonusPercent / 100.0));

        user.setTotalPp((long) calculatedPp);
        user.setTotalAttackChanceBonus(attackChanceBonus / 100.0);
        user.setTotalExtraAttacks(extraAttacks);
        //user.setPermanentCoinBonusPercent(coinBonusPercent / 100.0);

        profileRepository.updateUser(user);
    }

    // --- POMOĆNE METODE I GETTERI ---
    private void calculatePrices() {
        User user = currentUser.getValue();
        List<EquipmentItem> items = shopItems.getValue();
        if (user == null || items == null) return;
        int levelForPriceCalc = (user.getLevel() > 1) ? user.getLevel() - 1 : 1;
        long previousLevelReward = calculateBossRewardForLevel(levelForPriceCalc);
        Map<String, Long> priceMap = new HashMap<>();
        for (EquipmentItem item : items) {
            if (item.getType() == EquipmentType.WEAPON) continue;
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
    public LiveData<Map<String, Long>> getCalculatedPrices() { return calculatedPrices; }
    public LiveData<List<EquipmentItem>> getShopItems() { return shopItems; }
    public LiveData<List<UserEquipment>> getUserInventory() { return userInventory; }
    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<Event<String>> getToastMessage() { return toastMessage; }
    public void addShopItemForTesting(EquipmentItem item) { equipmentRepository.addShopItemForTesting(item); }
}
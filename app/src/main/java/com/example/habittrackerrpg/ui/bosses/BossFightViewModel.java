package com.example.habittrackerrpg.ui.bosses;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.Boss;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.EquipmentType;
import com.example.habittrackerrpg.data.model.Potion;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.example.habittrackerrpg.data.repository.AllianceRepository;
import com.example.habittrackerrpg.data.repository.BossRepository;
import com.example.habittrackerrpg.data.repository.EquipmentRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.logic.BattleRewards;
import com.example.habittrackerrpg.logic.BattleTurnResult;
import com.example.habittrackerrpg.logic.CalculateRewardsUseCase;
import com.example.habittrackerrpg.logic.CalculateUserStatsUseCase;
import com.example.habittrackerrpg.logic.BossFightUseCase;
import com.example.habittrackerrpg.logic.Event;
import com.example.habittrackerrpg.logic.GenerateBossUseCase;
import com.example.habittrackerrpg.logic.PotentialRewardsInfo;
import com.example.habittrackerrpg.ui.tasks.TaskViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class BossFightViewModel extends AndroidViewModel {

    private static final String TAG = "BossFightViewModel";

    private ProfileRepository profileRepository;
    private BossRepository bossRepository;

    private EquipmentRepository equipmentRepository;
    // Use Cases
    private GenerateBossUseCase generateBossUseCase;
    private CalculateUserStatsUseCase calculateUserStatsUseCase;
    private BossFightUseCase bossFightUseCase;
    private CalculateRewardsUseCase calculateRewardsUseCase;

    private MutableLiveData<Boss> _boss = new MutableLiveData<>();
    public LiveData<Boss> boss = _boss;
    private MutableLiveData<Long> _currentBossHp = new MutableLiveData<>();
    public LiveData<Long> currentBossHp = _currentBossHp;
    private MutableLiveData<Long> _userPp = new MutableLiveData<>();
    public LiveData<Long> userPp = _userPp;
    private MutableLiveData<Integer> _hitChance = new MutableLiveData<>();
    public LiveData<Integer> hitChance = _hitChance;
    private MutableLiveData<Integer> _attacksRemaining = new MutableLiveData<>();
    public LiveData<Integer> attacksRemaining = _attacksRemaining;
    private MutableLiveData<Event<BattleTurnResult.AttackResult>> _attackResultEvent = new MutableLiveData<>();
    public LiveData<Event<BattleTurnResult.AttackResult>> attackResultEvent = _attackResultEvent;
    private MutableLiveData<Event<BattleRewards>> _battleRewardsEvent = new MutableLiveData<>();
    public LiveData<Event<BattleRewards>> battleRewardsEvent = _battleRewardsEvent;
    private MutableLiveData<PotentialRewardsInfo> _potentialRewards = new MutableLiveData<>();
    public LiveData<PotentialRewardsInfo> potentialRewards = _potentialRewards; // Dodao sam public LiveData
    private LiveData<List<Boss>> allBossesLiveData;
    private User currentUser;
    private Boss currentBoss;
    private long initialBossHp;
    private MutableLiveData<Boolean> _isBattleOver = new MutableLiveData<>(false);
    public LiveData<Boolean> isBattleOver = _isBattleOver;
    private final LiveData<List<EquipmentItem>> allEquipmentShopItems;
    private final LiveData<List<EquipmentItem>> allEquipmentItems;
    private final LiveData<List<UserEquipment>> userInventory;

    private final AllianceRepository allianceRepository;
    private MutableLiveData<Integer> _maxAttacks = new MutableLiveData<>();
    public LiveData<Integer> maxAttacks = _maxAttacks;

    public BossFightViewModel(@NonNull Application application) {
        super(application);
        profileRepository = new ProfileRepository();
        allianceRepository = new AllianceRepository(application.getApplicationContext());
        bossRepository = new BossRepository();
        equipmentRepository = new EquipmentRepository(application.getApplicationContext());
        allBossesLiveData = bossRepository.getAllBosses();
        generateBossUseCase = new GenerateBossUseCase();
        calculateUserStatsUseCase = new CalculateUserStatsUseCase();
        bossFightUseCase = new BossFightUseCase();
        calculateRewardsUseCase = new CalculateRewardsUseCase();
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUid != null) {
            this.allEquipmentShopItems = equipmentRepository.getClothingAndWeapons();
            this.allEquipmentItems = equipmentRepository.getAllEquipmentItems();
            this.userInventory = equipmentRepository.getActiveUserInventory(currentUid);

        } else {
            this.allEquipmentShopItems = new MutableLiveData<>(Collections.emptyList());
            this.allEquipmentItems = new MutableLiveData<>(Collections.emptyList());
            this.userInventory = new MutableLiveData<>(Collections.emptyList());
            Log.e(TAG, "User is not logged in, cannot fetch equipment data.");
        }
    }

    public LiveData<List<EquipmentItem>> getAllEquipmentItems() {
        return allEquipmentShopItems;
    }

    public LiveData<List<EquipmentItem>> getAllEquipment() {
        return allEquipmentItems;
    }
    public LiveData<List<UserEquipment>> getUserInventory() {
        return userInventory;
    }

    public LiveData<List<Boss>> getAllBosses() {
        return allBossesLiveData;
    }

    public void startFight(User user, List<Boss> allBosses, List<Task> allTasks, List<TaskInstance> allInstances) {
        this.currentUser = user;

        this.currentBoss = generateBossUseCase.execute(user, allBosses, this.bossRepository);
        _boss.setValue(this.currentBoss);

        if (this.currentBoss == null) {
            Log.e(TAG, "Current boss is null, cannot start fight.");
            return;
        }

        Log.d(TAG, "--- Calculating Potential Rewards ---");
        List<EquipmentItem> allItems = allEquipmentShopItems.getValue();
        Log.d(TAG, "allEquipmentItems list size: " + (allItems != null ? allItems.size() : "null"));

        if (allItems != null && !allItems.isEmpty()) {
            long maxCoins = calculateRewardsUseCase.calculateBaseCoinsForBoss(this.currentBoss.getLevel());
            Log.d(TAG, "Calculated max potential coins: " + maxCoins);

            List<String> potentialRewardIcons = allItems.stream()
                    .filter(item -> item.getType() == EquipmentType.CLOTHING || item.getType() == EquipmentType.WEAPON)
                    .map(EquipmentItem::getIcon)
                    .collect(Collectors.toList());

            Log.d(TAG, "Total potential reward icons found: " + potentialRewardIcons.size());
            Log.d(TAG, "Setting potential rewards. Coins: " + maxCoins + ", Icons: " + potentialRewardIcons.toString());
            _potentialRewards.setValue(new PotentialRewardsInfo(String.valueOf(maxCoins), potentialRewardIcons));
        } else {
            Log.w(TAG, "Cannot calculate potential rewards because allEquipmentItems is null or empty.");
        }

        this.initialBossHp = this.currentBoss.getHp();
        _currentBossHp.setValue(this.initialBossHp);

        long calculatedPp = user.getTotalPp();
        _userPp.setValue(calculatedPp);

        int baseHitChance = user.getLastStageHitChance();
        int bonusHitChance = (int) (user.getTotalAttackChanceBonus() * 100);
        _hitChance.setValue(baseHitChance + bonusHitChance);
        int maxAttacks = 5 + user.getTotalExtraAttacks();
        _attacksRemaining.setValue(maxAttacks);
        _maxAttacks.setValue(maxAttacks);
    }

    public void performAttack() {
        Integer attacksLeft = _attacksRemaining.getValue();
        if (attacksLeft == null || attacksLeft <= 0) return;
        BattleTurnResult turnResult = bossFightUseCase.executeAttack(_userPp.getValue(), _hitChance.getValue());
        _attackResultEvent.setValue(new Event<>(turnResult.getResult()));
        long newHp = _currentBossHp.getValue();
        if (turnResult.wasHit()) {
            newHp = _currentBossHp.getValue() - turnResult.getDamageDealt();
            _currentBossHp.setValue(newHp > 0 ? newHp : 0);
            allianceRepository.logMissionAction("REGULAR_BOSS_HIT", 2);
        }

        int newAttacksLeft = attacksLeft - 1;
        _attacksRemaining.setValue(newAttacksLeft);

        if (newHp <= 0 || newAttacksLeft == 0) {
            finishBattle();
        }
    }

    private void finishBattle() {
        long remainingHp = _currentBossHp.getValue();
        List<EquipmentItem> allItems = allEquipmentShopItems.getValue();
        if (allItems == null) {
            allItems = new ArrayList<>();
        }
        BattleRewards rewards = calculateRewardsUseCase.execute(currentBoss, initialBossHp, remainingHp, allItems, currentUser);
        if (rewards.getCoinsAwarded() > 0) {
            profileRepository.addCoins(rewards.getCoinsAwarded());
        }
        if (remainingHp <= 0) {
            profileRepository.updateUserAfterBossVictory(currentBoss.getLevel());
        }
        updateEquipmentAfterBattle();
        _battleRewardsEvent.setValue(new Event<>(rewards));
        _isBattleOver.setValue(true);
        profileRepository.recordBossFightAttempt(currentBoss.getLevel() + 1);
    }

    public void resetBattleState() {
        _boss.setValue(null);
        _isBattleOver.setValue(false);
        _potentialRewards.setValue(null);
    }
    private void updateEquipmentAfterBattle() {
        List<UserEquipment> activeInventory = userInventory.getValue();
        List<EquipmentItem> allItemsDefinitions = allEquipmentItems.getValue();

        if (activeInventory == null || activeInventory.isEmpty() || allItemsDefinitions == null) {
            Log.d(TAG, "No active equipment to update after battle.");
            return;
        }

        Log.d(TAG, "Updating durability of active equipment after battle...");

        Map<String, EquipmentItem> definitionsMap = allItemsDefinitions.stream()
                .collect(Collectors.toMap(EquipmentItem::getId, item -> item));

        for (UserEquipment equipment : activeInventory) {
            if (!equipment.isActive()) continue;

            EquipmentItem definition = definitionsMap.get(equipment.getEquipmentId());
            if (definition == null) continue;

            if (definition.getType() == EquipmentType.POTION) {
                Potion potionDef = (Potion) definition;
                if (!potionDef.isPermanent()) {
                    Log.d(TAG, "Consumed single-use potion: " + definition.getName());
                    equipmentRepository.deleteUserEquipment(equipment.getId(), success -> {
                        if (success) Log.d(TAG, "Successfully deleted potion from inventory.");
                    });
                }
            }
            else if (definition.getType() == EquipmentType.CLOTHING) {
                int remaining = equipment.getBattlesRemaining() - 1;
                equipment.setBattlesRemaining(remaining);

                if (remaining <= 0) {
                    Log.d(TAG, "Clothing item durability depleted: " + definition.getName());
                    equipmentRepository.deleteUserEquipment(equipment.getId(), success -> {
                        if (success) Log.d(TAG, "Successfully deleted clothing from inventory.");
                    });
                }
                else {
                    Log.d(TAG, "Updating clothing durability for " + definition.getName() + ". Remaining: " + remaining);
                    equipmentRepository.updateUserEquipment(equipment, success -> {
                        if (success) Log.d(TAG, "Successfully updated clothing durability.");
                    });
                }
            }
        }
    }
}
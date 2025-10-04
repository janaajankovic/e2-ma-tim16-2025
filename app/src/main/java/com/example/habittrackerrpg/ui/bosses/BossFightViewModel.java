package com.example.habittrackerrpg.ui.bosses;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.Boss;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.Potion;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment;
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
import com.example.habittrackerrpg.ui.tasks.TaskViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BossFightViewModel extends ViewModel {

    private static final String TAG = "BossFightViewModel";

    private ProfileRepository profileRepository;
    private BossRepository bossRepository;

    private EquipmentRepository equipmentRepository;
    // Use Cases
    private GenerateBossUseCase generateBossUseCase;
    private CalculateUserStatsUseCase calculateUserStatsUseCase;
    private BossFightUseCase bossFightUseCase;
    private CalculateRewardsUseCase calculateRewardsUseCase;

    private TaskViewModel taskViewModel;
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

    private LiveData<List<Boss>> allBossesLiveData;

    private User currentUser;
    private Boss currentBoss;
    private long initialBossHp;
    private MutableLiveData<Boolean> _isBattleOver = new MutableLiveData<>(false);
    public LiveData<Boolean> isBattleOver = _isBattleOver;
    private final LiveData<List<EquipmentItem>> allEquipmentItems;
    private final LiveData<List<UserEquipment>> userInventory;

    public BossFightViewModel() {
        profileRepository = new ProfileRepository();
        bossRepository = new BossRepository();
        equipmentRepository = new EquipmentRepository();
        allBossesLiveData = bossRepository.getAllBosses();
        generateBossUseCase = new GenerateBossUseCase();
        calculateUserStatsUseCase = new CalculateUserStatsUseCase();
        bossFightUseCase = new BossFightUseCase();
        calculateRewardsUseCase = new CalculateRewardsUseCase();
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUid != null) {
            this.allEquipmentItems = equipmentRepository.getShopItems();
            this.userInventory = equipmentRepository.getActiveUserInventory(currentUid);
        } else {
            this.allEquipmentItems = new MutableLiveData<>(Collections.emptyList());
            this.userInventory = new MutableLiveData<>(Collections.emptyList());
            Log.e(TAG, "User is not logged in, cannot fetch equipment data.");
        }
    }

    public LiveData<List<EquipmentItem>> getAllEquipmentItems() {
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
            return;
        }

        this.initialBossHp = this.currentBoss.getHp();
        _currentBossHp.setValue(this.initialBossHp);
        List<UserEquipment> inventory = userInventory.getValue();
        List<EquipmentItem> allItems = allEquipmentItems.getValue();
        long calculatedPp = calculateTotalUserPp(user, inventory, allItems);
        _userPp.setValue(calculatedPp);
        _hitChance.setValue(user.getLastStageHitChance());
        _attacksRemaining.setValue(5);
    }

    public void performAttack() {
        Integer attacksLeft = _attacksRemaining.getValue();
        if (attacksLeft == null || attacksLeft <= 0) return;

        BattleTurnResult turnResult = bossFightUseCase.executeAttack(
                _userPp.getValue(),
                _hitChance.getValue()
        );

        _attackResultEvent.setValue(new Event<>(turnResult.getResult()));

        if (turnResult.wasHit()) {
            long newHp = _currentBossHp.getValue() - turnResult.getDamageDealt();
            _currentBossHp.setValue(newHp > 0 ? newHp : 0);
        }

        int newAttacksLeft = attacksLeft - 1;
        _attacksRemaining.setValue(newAttacksLeft);

        if (newAttacksLeft == 0) {
            finishBattle();
        }
    }

    private void finishBattle() {
        long remainingHp = _currentBossHp.getValue();
        List<EquipmentItem> allItems = allEquipmentItems.getValue();
        if (allItems == null) {
            allItems = new ArrayList<>();
        }

        BattleRewards rewards = calculateRewardsUseCase.execute(currentBoss, initialBossHp, remainingHp, allItems);
        if (rewards.getCoinsAwarded() > 0) {
            profileRepository.addCoins(rewards.getCoinsAwarded());
        }

        if (remainingHp <= 0) {
            profileRepository.updateUserAfterBossVictory(currentBoss.getLevel());
        }

        _battleRewardsEvent.setValue(new Event<>(rewards));
        _isBattleOver.setValue(true);
        profileRepository.recordBossFightAttempt(currentUser.getLevel());

    }

    private long calculateTotalUserPp(User user, List<UserEquipment> userEquipmentList, List<EquipmentItem> allEquipmentItems) {
        AtomicLong totalPp = new AtomicLong(user.getPp());
        Log.d(TAG, "Calculating PP. Base PP: " + totalPp);

        if (userEquipmentList == null || allEquipmentItems == null) return totalPp.get();

        for (UserEquipment ownedItem : userEquipmentList) {
            if (ownedItem.isActive()) {
                allEquipmentItems.stream()
                        .filter(def -> def.getId().equals(ownedItem.getEquipmentId()))
                        .findFirst()
                        .ifPresent(equipmentDef -> {
                            if (equipmentDef instanceof Potion) {
                                Potion potion = (Potion) equipmentDef;
                                long bonus = (long) (user.getPp() * (potion.getPpBoostPercent() / 100.0));
                                Log.d(TAG, "Applying active Potion '" + potion.getName() + "' bonus: +" + bonus + " PP");
                                totalPp.addAndGet(bonus);
                            }

                        });
            }
        }
        Log.d(TAG, "Final calculated PP with equipment: " + totalPp);
        return totalPp.get();
    }
}
package com.example.habittrackerrpg.ui.bosses;

import android.animation.Animator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Boss;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.model.UserEquipment;
import com.example.habittrackerrpg.databinding.FragmentBossFightBinding;
import com.example.habittrackerrpg.logic.BattleRewards;
import com.example.habittrackerrpg.logic.BattleTurnResult;
import com.example.habittrackerrpg.logic.PotentialRewardsInfo;
import com.example.habittrackerrpg.logic.ShakeDetector;
import com.example.habittrackerrpg.ui.tasks.TaskViewModel;

import java.util.List;

public class BossFightFragment extends Fragment {

    private FragmentBossFightBinding binding;
    private BossFightViewModel bossFightViewModel;
    private TaskViewModel taskViewModel;
    private ShakeDetector shakeDetector;
    private String currentBossIdleAnimation = null;
    private BattleRewards finalRewards = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBossFightBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bossFightViewModel = new ViewModelProvider(requireActivity()).get(BossFightViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        setupShakeDetector();
        setupClickListeners();
        showLoadingState();
        setupInitialDataObserver();
        observeViewModel();
    }

    private void setupInitialDataObserver() {
        MediatorLiveData<Boolean> isDataReady = new MediatorLiveData<>();

        LiveData<User> userSource = taskViewModel.getUserLiveData();
        LiveData<List<Boss>> bossesSource = bossFightViewModel.getAllBosses();
        LiveData<List<Task>> tasksSource = taskViewModel.getTaskRules();
        LiveData<List<TaskInstance>> instancesSource = taskViewModel.getTaskInstances();
        LiveData<List<EquipmentItem>> equipmentItemsSource = bossFightViewModel.getAllEquipmentItems();
        LiveData<List<UserEquipment>> inventorySource = bossFightViewModel.getUserInventory();
        LiveData<List<EquipmentItem>> equipmentItemsNOVASource = bossFightViewModel.getAllEquipment();

        Runnable checkDataReady = () -> {
            isDataReady.setValue(
                    userSource.getValue() != null &&
                            bossesSource.getValue() != null &&
                            tasksSource.getValue() != null &&
                            instancesSource.getValue() != null &&
                            equipmentItemsSource.getValue() != null &&
                            inventorySource.getValue() != null &&
                            equipmentItemsNOVASource.getValue() != null
            );
        };

        isDataReady.addSource(userSource, user -> checkDataReady.run());
        isDataReady.addSource(bossesSource, bosses -> checkDataReady.run());
        isDataReady.addSource(tasksSource, tasks -> checkDataReady.run());
        isDataReady.addSource(instancesSource, instances -> checkDataReady.run());
        isDataReady.addSource(equipmentItemsSource, items -> checkDataReady.run());
        isDataReady.addSource(inventorySource, inventory -> checkDataReady.run());
        isDataReady.addSource(equipmentItemsNOVASource, itemsNove -> checkDataReady.run());

        isDataReady.observe(getViewLifecycleOwner(), ready -> {
            if (ready != null && ready) {
                bossFightViewModel.startFight(
                        userSource.getValue(),
                        bossesSource.getValue(),
                        tasksSource.getValue(),
                        instancesSource.getValue()
                );
                isDataReady.removeSource(userSource);
                isDataReady.removeSource(bossesSource);
                isDataReady.removeSource(tasksSource);
                isDataReady.removeSource(instancesSource);
                isDataReady.removeSource(equipmentItemsSource);
                isDataReady.removeSource(inventorySource);
            }
        });
    }

    private void observeViewModel() {
        bossFightViewModel.boss.observe(getViewLifecycleOwner(), boss -> {
            if (boss != null) {
                showFightState();
                binding.textBossName.setText(boss.getName());
                binding.progressBarBossHp.setMax((int) boss.getHp());

                currentBossIdleAnimation = boss.getLottieAnimationName();
                playAnimation(currentBossIdleAnimation, true);
            } else {
                showNoBossState();
            }
        });

        bossFightViewModel.currentBossHp.observe(getViewLifecycleOwner(), hp -> {
            binding.progressBarBossHp.setProgress(hp.intValue(), true);
            binding.textBossHpValue.setText(String.format("%d / %d", hp.intValue(), binding.progressBarBossHp.getMax()));
        });

        bossFightViewModel.maxAttacks.observe(getViewLifecycleOwner(), max -> {
            Integer remaining = bossFightViewModel.attacksRemaining.getValue();
            if (remaining != null) {
                binding.textAttacksRemaining.setText(String.format("%d / %d", remaining, max));
            }
        });

        bossFightViewModel.attacksRemaining.observe(getViewLifecycleOwner(), attacks -> {
            Integer max = bossFightViewModel.maxAttacks.getValue();
            if (max != null) {
                binding.textAttacksRemaining.setText(String.format("%d / %d", attacks, max));
            } else {
                binding.textAttacksRemaining.setText(String.format("%d / ?", attacks));
            }
        });

        bossFightViewModel.userPp.observe(getViewLifecycleOwner(), pp -> {
            if (pp != null && pp > 0) {
                binding.progressBarUserPp.setMax(pp.intValue());
                binding.progressBarUserPp.setProgress(pp.intValue());
                binding.textUserPpValue.setText(String.valueOf(pp));
            } else {
                binding.progressBarUserPp.setMax(100);
                binding.progressBarUserPp.setProgress(0);
                binding.textUserPpValue.setText("0");
            }
        });

        bossFightViewModel.hitChance.observe(getViewLifecycleOwner(), chance -> {
            binding.textHitChance.setText(String.format("%d%%", chance));
        });

        bossFightViewModel.attackResultEvent.observe(getViewLifecycleOwner(), event -> {
            BattleTurnResult.AttackResult result = event.getContentIfNotHandled();
            if (result != null) {
                if (result == BattleTurnResult.AttackResult.HIT) {
                    playHitAnimation();
                } else {
                    playMissAnimation();
                }
            }
        });

        bossFightViewModel.battleRewardsEvent.observe(getViewLifecycleOwner(), event -> {
            BattleRewards rewards = event.getContentIfNotHandled();
            if (rewards != null) {
                showRewardsState(rewards);
            }
        });

        bossFightViewModel.getAllEquipment().observe(getViewLifecycleOwner(), allItems -> {
            List<UserEquipment> inventory = bossFightViewModel.getUserInventory().getValue();
            displayActiveEquipment(inventory, allItems);
        });

        bossFightViewModel.getUserInventory().observe(getViewLifecycleOwner(), inventory -> {
            String TAG = "InventoryLog";
            if (inventory == null) {
                Log.d(TAG, "Active inventory is null.");
            } else {
                Log.d(TAG, "Active inventory updated. Count: " + inventory.size());
                for (UserEquipment item : inventory) {
                    Log.d(TAG, " -> Item - EquipmentID: " + item.getEquipmentId() + ", Type: " + item.getType());
                }
            }

            List<EquipmentItem> allItems = bossFightViewModel.getAllEquipment().getValue();

            String allItemsTag = "AllItemsLog";
            if (allItems == null) {
                Log.d(allItemsTag, "List of all equipment definitions is null.");
            } else {
                Log.d(allItemsTag, "List of all equipment definitions updated. Count: " + allItems.size());
                for (EquipmentItem item : allItems) {
                    Log.d(allItemsTag, " -> Definition - ID: " + item.getId() + ", Name: " + item.getName() + ", Type: " + item.getType());
                }
            }

            displayActiveEquipment(inventory, allItems);
        });

        bossFightViewModel.potentialRewards.observe(getViewLifecycleOwner(), rewardsInfo -> {
            displayPotentialRewards(rewardsInfo);
        });
    }

    private void setupShakeDetector() {
        shakeDetector = new ShakeDetector(requireContext());
        shakeDetector.setOnShakeListener(() -> {
            Boolean isBattleFinished = bossFightViewModel.isBattleOver.getValue();
            if (isBattleFinished != null && isBattleFinished  && finalRewards != null) {
                if (!binding.lottieTreasureChest.isAnimating()) {
                    try {
                        final MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.chest_sound);
                        mediaPlayer.setOnCompletionListener(mp -> mp.release());
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    binding.lottieTreasureChest.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationEnd(@NonNull Animator animation) {
                            displayFinalRewards(finalRewards);

                            binding.lottieTreasureChest.removeAnimatorListener(this);
                        }

                        @Override public void onAnimationStart(@NonNull Animator animation) {}
                        @Override public void onAnimationCancel(@NonNull Animator animation) {}
                        @Override public void onAnimationRepeat(@NonNull Animator animation) {}
                    });

                    binding.lottieTreasureChest.playAnimation();
                }
            } else if (binding.buttonAttack.getVisibility() == View.VISIBLE) {
                bossFightViewModel.performAttack();
            }
        });
    }

    private void displayFinalRewards(BattleRewards rewards) {
        binding.textFinalRewardsInfo.setText("You won " + rewards.getCoinsAwarded() + " Coins!");
        binding.textFinalRewardsInfo.setVisibility(View.VISIBLE);

        if (rewards.getEquipmentAwarded() != null) {
            EquipmentItem droppedItem = rewards.getEquipmentAwarded();
            binding.textRewardEquipmentName.setText(droppedItem.getName());
            binding.textRewardEquipmentName.setVisibility(View.VISIBLE);
            binding.imageRewardEquipment.setVisibility(View.VISIBLE);

            int iconResId = getResources().getIdentifier(
                    droppedItem.getIcon(), "drawable", requireActivity().getPackageName()
            );
            if (iconResId != 0) {
                binding.imageRewardEquipment.setImageResource(iconResId);
            } else {
                binding.imageRewardEquipment.setImageResource(R.drawable.ic_swords);
            }
        } else {
            binding.textRewardEquipmentName.setVisibility(View.GONE);
            binding.imageRewardEquipment.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        binding.buttonAttack.setOnClickListener(v -> bossFightViewModel.performAttack());
    }

    private void displayPotentialRewards(PotentialRewardsInfo rewardsInfo) {
        if (binding == null || rewardsInfo == null) return;
        binding.textPotentialCoins.setText(rewardsInfo.getMaxCoins());
        binding.potentialEquipmentIcons.removeAllViews();
        for (String iconName : rewardsInfo.getRepresentativeItemIcons()) {
            ImageView iconView = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(72, 72);
            params.setMarginEnd(8);
            iconView.setLayoutParams(params);
            int iconResId = getResources().getIdentifier(iconName, "drawable", requireActivity().getPackageName());
            if (iconResId != 0) {
                iconView.setImageResource(iconResId);
            } else {
                iconView.setImageResource(R.drawable.ic_cancel);
            }
            binding.potentialEquipmentIcons.addView(iconView);
        }
    }

    private void displayActiveEquipment(List<UserEquipment> inventory, List<EquipmentItem> allItems) {
        if (binding == null) return;
        binding.activeEquipmentContainer.removeAllViews();
        if (inventory == null || allItems == null || inventory.isEmpty()) {
            return;
        }
        for (UserEquipment ownedItem : inventory) {
            allItems.stream()
                    .filter(item -> item.getId().equals(ownedItem.getEquipmentId()))
                    .findFirst()
                    .ifPresent(itemDetails -> {
                        ImageView iconView = new ImageView(getContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
                        params.setMarginEnd(16);
                        iconView.setLayoutParams(params);
                        int iconResId = getResources().getIdentifier(
                                itemDetails.getIcon(), "drawable", requireActivity().getPackageName()
                        );
                        if (iconResId != 0) {
                            iconView.setImageResource(iconResId);
                        } else {
                            iconView.setImageResource(R.drawable.ic_shield);
                        }
                        binding.activeEquipmentContainer.addView(iconView);
                    });
        }
    }

    private void showFightState() {
        binding.fightUiContainer.setVisibility(View.VISIBLE);
        binding.rewardsUiContainer.setVisibility(View.GONE);
        binding.buttonAttack.setVisibility(View.VISIBLE);
    }

    private void showLoadingState() {
        binding.fightUiContainer.setVisibility(View.INVISIBLE);
        binding.rewardsUiContainer.setVisibility(View.GONE);
        binding.textBossName.setText("Loading boss...");
        binding.buttonAttack.setVisibility(View.INVISIBLE);
    }

    private void showNoBossState() {
        binding.fightUiContainer.setVisibility(View.INVISIBLE);
        binding.rewardsUiContainer.setVisibility(View.GONE);
        binding.textBossName.setText("No bosses to fight. Keep completing tasks!");
    }

    private void showRewardsState(BattleRewards rewards) {
        this.finalRewards = rewards;

        binding.fightUiContainer.setVisibility(View.GONE);
        binding.rewardsUiContainer.setVisibility(View.VISIBLE);

        binding.textRewardsTitle.setText("Battle Over!");
        binding.lottieTreasureChest.setVisibility(View.VISIBLE);

        binding.textFinalRewardsInfo.setVisibility(View.GONE);
        binding.imageRewardEquipment.setVisibility(View.GONE);
        binding.textRewardEquipmentName.setVisibility(View.GONE);
    }

    private void playAnimation(String fileName, boolean loop) {
        if (fileName == null || fileName.isEmpty()) return;
        binding.lottieBossAnimation.setAnimation(fileName);
        binding.lottieBossAnimation.setRepeatCount(loop ? com.airbnb.lottie.LottieDrawable.INFINITE : 0);
        binding.lottieBossAnimation.playAnimation();
    }

    private void playHitAnimation() {
        playAnimation("boxing.json", false);
        binding.lottieBossAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(@NonNull Animator animation) {}
            @Override public void onAnimationEnd(@NonNull Animator animation) {
                if (currentBossIdleAnimation != null) playAnimation(currentBossIdleAnimation, true);
                binding.lottieBossAnimation.removeAnimatorListener(this);
            }
            @Override public void onAnimationCancel(@NonNull Animator animation) {
                if (currentBossIdleAnimation != null) playAnimation(currentBossIdleAnimation, true);
            }
            @Override public void onAnimationRepeat(@NonNull Animator animation) {}
        });
    }

    private void playMissAnimation() {
        Toast.makeText(getContext(), "Miss!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        shakeDetector.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        shakeDetector.stop();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bossFightViewModel != null) {
            bossFightViewModel.resetBattleState();
        }
        binding = null;
    }
}
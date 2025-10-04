package com.example.habittrackerrpg.ui.bosses;

import android.animation.Animator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Boss;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.databinding.FragmentBossFightBinding;
import com.example.habittrackerrpg.logic.BattleRewards;
import com.example.habittrackerrpg.logic.BattleTurnResult;
import com.example.habittrackerrpg.logic.ShakeDetector;
import com.example.habittrackerrpg.ui.tasks.TaskViewModel;

import java.util.List;

public class BossFightFragment extends Fragment {

    private FragmentBossFightBinding binding;
    private BossFightViewModel bossFightViewModel;
    private TaskViewModel taskViewModel;
    private ShakeDetector shakeDetector;
    private String currentBossIdleAnimation = null;

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

        Runnable checkDataReady = () -> {
            isDataReady.setValue(
                    userSource.getValue() != null &&
                            bossesSource.getValue() != null &&
                            tasksSource.getValue() != null &&
                            instancesSource.getValue() != null
            );
        };

        isDataReady.addSource(userSource, user -> checkDataReady.run());
        isDataReady.addSource(bossesSource, bosses -> checkDataReady.run());
        isDataReady.addSource(tasksSource, tasks -> checkDataReady.run());
        isDataReady.addSource(instancesSource, instances -> checkDataReady.run());

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

        bossFightViewModel.attacksRemaining.observe(getViewLifecycleOwner(), attacks -> {
            binding.textAttacksRemaining.setText(String.format("%d / 5", attacks));
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

        bossFightViewModel.hitChance.observe(getViewLifecycleOwner(), chance -> binding.textHitChance.setText(String.format("%d%%", chance)));

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
    }

    private void setupShakeDetector() {
        shakeDetector = new ShakeDetector(requireContext());
        shakeDetector.setOnShakeListener(() -> {
            Boolean isBattleFinished = bossFightViewModel.isBattleOver.getValue();
            if (isBattleFinished != null && isBattleFinished) {

                if (!binding.lottieTreasureChest.isAnimating()) {
                    try {
                        final MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.chest_sound);

                        mediaPlayer.setOnCompletionListener(mp -> mp.release());

                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    binding.lottieTreasureChest.playAnimation();
                }
            }
            else if (binding.buttonAttack.getVisibility() == View.VISIBLE) {
                bossFightViewModel.performAttack();
            }
        });
    }

    private void setupClickListeners() {
        binding.buttonAttack.setOnClickListener(v -> bossFightViewModel.performAttack());
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
            @Override
            public void onAnimationStart(@NonNull Animator animation) {}

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if (currentBossIdleAnimation != null) {
                    playAnimation(currentBossIdleAnimation, true);
                }
                binding.lottieBossAnimation.removeAnimatorListener(this);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                if (currentBossIdleAnimation != null) {
                    playAnimation(currentBossIdleAnimation, true);
                }
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });
    }

    private void playMissAnimation() {
        Toast.makeText(getContext(), "Miss!", Toast.LENGTH_SHORT).show();
    }

    private void showLoadingState() {
        binding.textBossName.setText("Loading boss...");
        binding.infoContainer.setVisibility(View.INVISIBLE);
        binding.buttonAttack.setVisibility(View.INVISIBLE);
        binding.groupRewards.setVisibility(View.GONE);
    }

    private void showNoBossState() {
        binding.lottieBossAnimation.setVisibility(View.INVISIBLE);
        binding.infoContainer.setVisibility(View.INVISIBLE);
        binding.buttonAttack.setVisibility(View.GONE);
        binding.groupRewards.setVisibility(View.GONE);
        binding.textBossName.setText("No bosses to fight. Keep completing tasks!");
        binding.progressBarBossHp.setVisibility(View.INVISIBLE);
        binding.textBossHpValue.setVisibility(View.INVISIBLE);
        binding.labelBossHp.setVisibility(View.INVISIBLE);
    }

    private void showFightState() {
        binding.lottieBossAnimation.setVisibility(View.VISIBLE);
        binding.infoContainer.setVisibility(View.VISIBLE);
        binding.buttonAttack.setVisibility(View.VISIBLE);
        binding.groupRewards.setVisibility(View.GONE);
        binding.progressBarBossHp.setVisibility(View.VISIBLE);
        binding.textBossHpValue.setVisibility(View.VISIBLE);
        binding.labelBossHp.setVisibility(View.VISIBLE);
    }

    private void showRewardsState(BattleRewards rewards) {
        showNoBossState();
        binding.groupRewards.setVisibility(View.VISIBLE);
        binding.textRewardsTitle.setText("Coins won: " + rewards.getCoinsAwarded());
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
        binding = null;
    }
}
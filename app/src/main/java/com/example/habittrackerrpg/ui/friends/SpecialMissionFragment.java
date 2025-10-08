package com.example.habittrackerrpg.ui.friends;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habittrackerrpg.databinding.FragmentSpecialMissionBinding;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SpecialMissionFragment extends Fragment {

    private FragmentSpecialMissionBinding binding;
    private SpecialMissionViewModel viewModel;
    private CountDownTimer countDownTimer;
    private MemberProgressAdapter memberProgressAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSpecialMissionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SpecialMissionViewModel.class);

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        memberProgressAdapter = new MemberProgressAdapter();
        binding.recyclerViewMemberProgress.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewMemberProgress.setAdapter(memberProgressAdapter);
    }

    private void observeViewModel() {
        viewModel.missionDetails.observe(getViewLifecycleOwner(), mission -> {
            if (mission == null) {
                return;
            }

            long progress = mission.getCurrentBossHp();
            long max = mission.getInitialBossHp();
            binding.progressBarMissionBossHp.setMax((int) max);
            binding.progressBarMissionBossHp.setProgress((int) progress);
            binding.textViewMissionBossHp.setText(String.format(Locale.getDefault(), "%d / %d", progress, max));

            long millisLeft = mission.getEndDate().getTime() - System.currentTimeMillis();
            if (millisLeft > 0) {
                startTimer(millisLeft);
            } else {
                binding.textViewTimeRemaining.setText("Mission Finished!");
            }
        });

        viewModel.myProgress.observe(getViewLifecycleOwner(), progress -> {
            if (progress == null) return;
            binding.textViewMyDamage.setText("Total Damage: " + progress.getTotalDamageDealt() + " HP");
            binding.textViewMyShop.setText("Shop Purchases: " + progress.getShopPurchases() + "/5");
            binding.textViewMyBossHits.setText("Regular Boss Hits: " + progress.getRegularBossHits() + "/10");
            binding.textViewMyTaskCompletions.setText("Task Completions: " + progress.getTaskCompletions() + "/10");
        });

        viewModel.allMembersProgress.observe(getViewLifecycleOwner(), progressList -> {
            if (progressList != null) {
                memberProgressAdapter.submitList(progressList);
            }
        });
    }

    private void startTimer(long millisInFuture) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;

                String time = String.format(Locale.getDefault(), "%dd %dh %dm", days, hours, minutes);
                binding.textViewTimeRemaining.setText("Time Remaining: " + time);
            }

            @Override
            public void onFinish() {
                binding.textViewTimeRemaining.setText("Mission Finished!");
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        binding = null;
    }
}
package com.example.habittrackerrpg.ui.statistics;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.habittrackerrpg.ui.tasks.TaskViewModel;

public class StatisticsViewModelFactory implements ViewModelProvider.Factory {
    private final TaskViewModel taskViewModel;

    public StatisticsViewModelFactory(TaskViewModel taskViewModel) {
        this.taskViewModel = taskViewModel;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StatisticsViewModel.class)) {
            return (T) new StatisticsViewModel(taskViewModel);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
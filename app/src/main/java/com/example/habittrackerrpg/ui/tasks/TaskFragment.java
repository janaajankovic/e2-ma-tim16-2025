package com.example.habittrackerrpg.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.databinding.FragmentTaskBinding;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskFragment extends Fragment implements TaskListAdapter.OnTaskActionListener {

    private FragmentTaskBinding binding;
    private TaskViewModel taskViewModel;
    private TaskListAdapter adapter;
    private Map<String, Category> categoriesById = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        setupRecyclerView();
        setupTabs();
        setupNavigationButtons();
        setupObservers();

        taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categories.forEach(c -> categoriesById.put(c.getId(), c));
                observeTasks(binding.tabLayoutTasks.getSelectedTabPosition());
            }
        });
    }


    private void setupObservers() {
        taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                Map<String, Category> categoriesMap = categories.stream()
                        .filter(c -> c.getId() != null)
                        .collect(Collectors.toMap(Category::getId, c -> c));
                adapter.setCategories(categoriesMap);
            }
        });

        observeTasks(binding.tabLayoutTasks.getSelectedTabPosition());
    }
    private void setupRecyclerView() {
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskListAdapter(new HashMap<>(), this);
        binding.recyclerViewTasks.setAdapter(adapter);
    }

    private void setupTabs() {
        binding.tabLayoutTasks.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                observeTasks(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void observeTasks(int tabPosition) {
        taskViewModel.getOneTimeTasks().removeObservers(getViewLifecycleOwner());
        taskViewModel.getRecurringTasks().removeObservers(getViewLifecycleOwner());

        if (tabPosition == 0) {
            taskViewModel.getOneTimeTasks().observe(getViewLifecycleOwner(), tasks -> {
                adapter.submitList(tasks);
            });
        } else {
            taskViewModel.getRecurringTasks().observe(getViewLifecycleOwner(), tasks -> {
                adapter.submitList(tasks);
            });
        }
    }

    private void setupNavigationButtons() {
        binding.fabAddTask.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_nav_tasks_to_createTaskFragment));

        binding.buttonManageCategories.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_nav_tasks_to_manageCategoriesFragment));

        binding.buttonCalendarView.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_nav_tasks_to_calendarViewFragment));
    }

    @Override
    public void onCompleteClick(Task task) {
        taskViewModel.updateTaskStatus(task, TaskStatus.COMPLETED);
    }

    @Override
    public void onCancelClick(Task task) {
        taskViewModel.updateTaskStatus(task, TaskStatus.CANCELLED);
    }

    @Override
    public void onTaskClick(Task task) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("task", task);
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_tasks_to_taskDetailFragment, bundle);
    }

    @Override
    public void onPauseClick(Task task) {
        TaskStatus newStatus = (task.getStatus() == TaskStatus.PAUSED) ? TaskStatus.ACTIVE : TaskStatus.PAUSED;
        taskViewModel.updateTaskStatus(task, newStatus);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
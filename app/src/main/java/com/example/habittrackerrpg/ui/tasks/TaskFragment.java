package com.example.habittrackerrpg.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskFragment extends Fragment implements TaskListAdapter.OnTaskActionListener {

    private FragmentTaskBinding binding;
    private TaskViewModel taskViewModel;
    private TaskListAdapter adapter;

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
    }

    private void setupRecyclerView() {
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskListAdapter(new HashMap<>(), this);
        binding.recyclerViewTasks.setAdapter(adapter);
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

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), allTasks -> {
            if (allTasks != null) {
                filterAndSubmitTasks(allTasks);
            }
        });
    }

    private void setupTabs() {
        binding.tabLayoutTasks.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (taskViewModel.getTasks().getValue() != null) {
                    filterAndSubmitTasks(taskViewModel.getTasks().getValue());
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterAndSubmitTasks(List<Task> allTasks) {
        int selectedTabPosition = binding.tabLayoutTasks.getSelectedTabPosition();
        List<Task> filteredTasks;
        Date today = getStartOfToday();

        if (selectedTabPosition == 0) {
            filteredTasks = allTasks.stream()
                    .filter(t -> !t.isRecurring() && t.getDueDate() != null && !t.getDueDate().before(today) && !t.getStatus().equals(TaskStatus.CANCELLED))
                    .collect(Collectors.toList());
        } else {
            filteredTasks = allTasks.stream()
                    .filter(t -> t.isRecurring() && t.getRecurrenceEndDate() != null && !t.getRecurrenceEndDate().before(today) && !t.getStatus().equals(TaskStatus.CANCELLED))
                    .collect(Collectors.toList());
        }
        adapter.submitList(filteredTasks);
    }

    private Date getStartOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
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
    public void onPauseClick(Task task) {
        TaskStatus newStatus = (task.getStatus() == TaskStatus.PAUSED) ? TaskStatus.ACTIVE : TaskStatus.PAUSED;
        taskViewModel.updateTaskStatus(task, newStatus);
    }


    @Override
    public void onTaskClick(Task task) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("task", task);
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_tasks_to_taskDetailFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
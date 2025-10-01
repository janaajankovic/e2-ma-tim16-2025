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
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.databinding.FragmentTaskBinding;
import com.example.habittrackerrpg.logic.GenerateTaskOccurrencesUseCase;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskFragment extends Fragment implements TaskListAdapter.OnTaskActionListener {

    private FragmentTaskBinding binding;
    private TaskViewModel taskViewModel;
    private TaskListAdapter adapter;
    private GenerateTaskOccurrencesUseCase generateOccurrencesUseCase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTaskBinding.inflate(inflater, container, false);
        generateOccurrencesUseCase = new GenerateTaskOccurrencesUseCase();
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

        taskViewModel.getTaskRules().observe(getViewLifecycleOwner(), rules -> refreshTaskList());
        taskViewModel.getTaskInstances().observe(getViewLifecycleOwner(), instances -> refreshTaskList());
    }

    private void setupTabs() {
        binding.tabLayoutTasks.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                refreshTaskList();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void refreshTaskList() {
        List<Task> taskRules = taskViewModel.getTaskRules().getValue();
        List<TaskInstance> taskInstances = taskViewModel.getTaskInstances().getValue();
        if (taskRules == null || taskInstances == null) {
            adapter.submitList(new ArrayList<>());
            return;
        }

        int selectedTabPosition = binding.tabLayoutTasks.getSelectedTabPosition();
        List<Task> tasksForDisplay;

        if (selectedTabPosition == 0) {
            Date today = getStartOfToday();
            tasksForDisplay = taskRules.stream()
                    .filter(t -> !t.isRecurring() && t.getDueDate() != null && !t.getDueDate().before(today) && t.getStatus() != TaskStatus.CANCELLED)
                    .collect(Collectors.toList());
        } else {
            tasksForDisplay = new ArrayList<>();

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(30);

            Map<String, TaskInstance> instanceMap = taskInstances.stream()
                    .collect(Collectors.toMap(
                            instance -> instance.getOriginalTaskId() + "_" + instance.getInstanceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                            instance -> instance,
                            (a, b) -> b
                    ));

            for (Task rule : taskRules) {
                if (rule.isRecurring() && rule.getStatus() != TaskStatus.CANCELLED) {
                    List<LocalDate> occurrences = generateOccurrencesUseCase.execute(rule, startDate, endDate);

                    for (LocalDate occurrenceDate : occurrences) {
                        String instanceKey = rule.getId() + "_" + occurrenceDate.toString();
                        TaskInstance instance = instanceMap.get(instanceKey);

                        if (instance != null && instance.getStatus() == TaskStatus.CANCELLED) {
                            continue;
                        }

                        Task virtualTask = new Task(rule);
                        java.time.LocalTime ruleTime = rule.getRecurrenceStartDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime();

                        java.time.LocalDateTime dateTimeForDisplay = occurrenceDate.atTime(ruleTime);

                        Date dateForDisplay = Date.from(dateTimeForDisplay.atZone(ZoneId.systemDefault()).toInstant());

                        virtualTask.setDueDate(dateForDisplay);

                        if (instance != null) {
                            virtualTask.setStatus(instance.getStatus());
                        } else if (rule.getStatus() == TaskStatus.PAUSED) {
                            virtualTask.setStatus(TaskStatus.PAUSED);
                        } else {
                            virtualTask.setStatus(TaskStatus.ACTIVE);
                        }

                        tasksForDisplay.add(virtualTask);
                    }
                }
            }
            tasksForDisplay.sort(Comparator.comparing(Task::getDueDate));
        }
        adapter.submitList(tasksForDisplay);
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
        binding.fabAddTask.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_nav_tasks_to_createTaskFragment));
        binding.buttonManageCategories.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_nav_tasks_to_manageCategoriesFragment));
        binding.buttonCalendarView.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_nav_tasks_to_calendarViewFragment));
    }
    @Override
    public void onCompleteClick(Task task) {
        taskViewModel.updateTaskStatus(task, TaskStatus.COMPLETED, task.getDueDate());
    }

    @Override
    public void onCancelClick(Task task) {
        taskViewModel.updateTaskStatus(task, TaskStatus.CANCELLED, task.getDueDate());
    }

    @Override
    public void onPauseClick(Task task) {
        TaskStatus newStatus = (task.getStatus() == TaskStatus.PAUSED) ? TaskStatus.ACTIVE : TaskStatus.PAUSED;
        taskViewModel.updateTaskStatus(task, newStatus, task.getDueDate());
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
package com.example.habittrackerrpg.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance; // NOVI IMPORT
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.databinding.BottomSheetDayTasksBinding;
import com.example.habittrackerrpg.logic.GenerateTaskOccurrencesUseCase;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DayTasksBottomSheetFragment extends BottomSheetDialogFragment implements DayTasksAdapter.OnTaskActionListener {

    private BottomSheetDayTasksBinding binding;
    private TaskViewModel taskViewModel;
    private DayTasksAdapter adapter;
    private LocalDate selectedDate;
    private GenerateTaskOccurrencesUseCase generateOccurrencesUseCase;

    private static final String ARG_DATE = "date";
    private static final String ARG_DATE_TITLE = "date_title";

    public interface TaskSelectionListener {
        void onTaskSelected(Task task);
    }
    private TaskSelectionListener selectionListener;

    public void setTaskSelectionListener(TaskSelectionListener listener) {
        this.selectionListener = listener;
    }

    public static DayTasksBottomSheetFragment newInstance(LocalDate date, String dateTitle) {
        DayTasksBottomSheetFragment fragment = new DayTasksBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        args.putString(ARG_DATE_TITLE, dateTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedDate = (LocalDate) getArguments().getSerializable(ARG_DATE);
        }
        generateOccurrencesUseCase = new GenerateTaskOccurrencesUseCase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetDayTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        binding.textBottomSheetTitle.setText(getArguments().getString(ARG_DATE_TITLE));

        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new DayTasksAdapter(new ArrayList<>(), new HashMap<>(), this);
        binding.recyclerViewDayTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewDayTasks.setAdapter(adapter);
    }

    private void setupObservers() {
        taskViewModel.getTaskRules().observe(getViewLifecycleOwner(), rules -> refreshTasks());
        taskViewModel.getTaskInstances().observe(getViewLifecycleOwner(), instances -> refreshTasks());
        taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> refreshTasks());
    }

    private void refreshTasks() {
        List<Task> taskRules = taskViewModel.getTaskRules().getValue();
        List<TaskInstance> taskInstances = taskViewModel.getTaskInstances().getValue();
        List<Category> allCategories = taskViewModel.getCategories().getValue();

        if (taskRules == null || taskInstances == null || allCategories == null || selectedDate == null) {
            adapter.updateData(new ArrayList<>(), new HashMap<>());
            return;
        }

        Map<String, TaskInstance> instancesForDateMap = taskInstances.stream()
                .filter(instance -> {
                    LocalDate instanceLocalDate = instance.getInstanceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return selectedDate.equals(instanceLocalDate);
                })
                .collect(Collectors.toMap(TaskInstance::getOriginalTaskId, instance -> instance, (a, b) -> b));

        List<Task> tasksForDisplay = new ArrayList<>();

        for (Task rule : taskRules) {
            if (rule.isRecurring()) {
                List<LocalDate> occurrences = generateOccurrencesUseCase.execute(rule, selectedDate, selectedDate);
                if (!occurrences.isEmpty()) {
                    TaskInstance instance = instancesForDateMap.get(rule.getId());
                    Task virtualTask = new Task(rule);

                    java.time.LocalTime ruleTime = rule.getRecurrenceStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
                    java.time.LocalDateTime dateTimeForDisplay = selectedDate.atTime(ruleTime);
                    virtualTask.setDueDate(Date.from(dateTimeForDisplay.atZone(ZoneId.systemDefault()).toInstant()));

                    if (instance != null) {
                        virtualTask.setStatus(instance.getStatus());
                    } else {
                        virtualTask.setStatus(rule.getStatus());
                    }
                    tasksForDisplay.add(virtualTask);
                }
            } else {
                if (rule.getDueDate() != null) {
                    LocalDate dueDate = rule.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (selectedDate.equals(dueDate)) {
                        tasksForDisplay.add(rule);
                    }
                }
            }
        }

        Map<String, Category> categoriesMap = allCategories.stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(Category::getId, c -> c));

        adapter.updateData(tasksForDisplay, categoriesMap);
    }

    private Date getOccurrenceDate() {
        return Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public void onCompleteClick(Task task) {
        taskViewModel.updateTaskStatus(task, TaskStatus.COMPLETED, getOccurrenceDate());
    }

    @Override
    public void onCancelClick(Task task) {
        taskViewModel.updateTaskStatus(task, TaskStatus.CANCELLED, getOccurrenceDate());
    }

    @Override
    public void onPauseClick(Task task) {
        TaskStatus newStatus = (task.getStatus() == TaskStatus.PAUSED) ? TaskStatus.ACTIVE : TaskStatus.PAUSED;
        taskViewModel.updateTaskStatus(task, newStatus, getOccurrenceDate());
    }

    @Override
    public void onTaskClick(Task task) {
        if (selectionListener != null) {
            selectionListener.onTaskSelected(task);
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
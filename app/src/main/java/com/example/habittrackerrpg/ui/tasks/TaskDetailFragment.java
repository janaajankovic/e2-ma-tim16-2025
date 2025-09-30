package com.example.habittrackerrpg.ui.tasks;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.databinding.FragmentTaskDetailBinding;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskDetailFragment extends Fragment {

    private FragmentTaskDetailBinding binding;
    private TaskViewModel taskViewModel;
    private String currentTaskId;

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Task initialTask = (Task) getArguments().getSerializable("task");
            if (initialTask != null) {
                currentTaskId = initialTask.getId();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        if (currentTaskId != null) {
            taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
                tasks.stream()
                        .filter(task -> currentTaskId.equals(task.getId()))
                        .findFirst()
                        .ifPresent(this::updateUiForTask);
            });
        }
    }

    private void updateUiForTask(Task task) {
        binding.textTaskDetailName.setText(task.getName());
        binding.textTaskDetailDescription.setText(task.getDescription());
        binding.textDifficultyInfo.setText("Difficulty: " + task.getDifficulty().name());
        binding.textImportanceInfo.setText("Importance: " + task.getImportance().name());
        binding.textXpValueInfo.setText("Value: +" + task.getXpValue() + " XP");

        populateScheduleInfo(task);
        populateCategoryInfo(task);
        updateActionButtons(task);
        setupListeners(task);
    }

    private void populateScheduleInfo(Task task) {
        if (task.isRecurring()) {
            String unit = task.getRecurrenceUnit();
            int interval = task.getRecurrenceInterval();
            String scheduleText = "Repeats every " + interval + " " + unit + " at " + timeFormat.format(task.getRecurrenceStartDate());
            binding.textScheduleInfo.setText(scheduleText);
        } else if (task.getDueDate() != null) {
            binding.textScheduleInfo.setText("Due: " + dateTimeFormat.format(task.getDueDate()));
        } else {
            binding.textScheduleInfo.setText("No schedule set");
        }
    }

    private void populateCategoryInfo(Task task) {
        if (taskViewModel.getCategories().getValue() == null) return;

        taskViewModel.getCategories().getValue().stream()
                .filter(category -> task.getCategoryId().equals(category.getId()))
                .findFirst()
                .ifPresent(category -> {
                    binding.textCategoryInfo.setText("Category: " + category.getName());
                    int categoryColor = Color.parseColor(category.getColor());
                    binding.textCategoryInfo.setCompoundDrawableTintList(ColorStateList.valueOf(categoryColor));
                });
    }

    private void updateActionButtons(Task task) {
        TaskStatus status = task.getStatus();
        binding.chipTaskStatus.setText(status.name());

        switch (status) {
            case COMPLETED:
                binding.chipTaskStatus.setChipBackgroundColorResource(R.color.action_color_done);
                break;
            case CANCELLED:
                binding.chipTaskStatus.setChipBackgroundColorResource(R.color.icon_tint_default);
                break;
            case PAUSED:
                binding.chipTaskStatus.setChipBackgroundColorResource(R.color.icon_color_on_action);
                break;
            case UNCOMPLETED:
                binding.chipTaskStatus.setChipBackgroundColorResource(R.color.action_color_cancel);
                break;
            case ACTIVE:
            default:
                binding.chipTaskStatus.setChipBackgroundColorResource(R.color.action_color_pause);
                break;
        }

        if (status == TaskStatus.COMPLETED || status == TaskStatus.UNCOMPLETED || status == TaskStatus.CANCELLED) {
            binding.actionsContainer.setVisibility(View.GONE);
            binding.buttonEditTask.setVisibility(View.GONE);
            binding.buttonDeleteTask.setVisibility(View.GONE);
            return;
        }

        binding.actionsContainer.setVisibility(View.VISIBLE);

        if (status == TaskStatus.ACTIVE) {
            binding.buttonMarkDone.setVisibility(View.VISIBLE);
            binding.buttonCancelTask.setVisibility(View.VISIBLE);

            if (task.isRecurring()) {
                binding.buttonPauseTask.setVisibility(View.VISIBLE);
                binding.buttonPauseTask.setText("Pause");
                binding.buttonPauseTask.setIconResource(R.drawable.ic_pause_circle);
            } else {
                binding.buttonPauseTask.setVisibility(View.GONE);
            }

        } else if (status == TaskStatus.PAUSED) {
            binding.buttonCancelTask.setVisibility(View.GONE);
            binding.buttonPauseTask.setVisibility(View.VISIBLE);
            binding.buttonPauseTask.setText("Activate");
            binding.buttonPauseTask.setIconResource(R.drawable.ic_play_circle);
            binding.buttonMarkDone.setVisibility(View.GONE);
        }
    }

    private void setupListeners(Task task) {
        binding.buttonMarkDone.setOnClickListener(v -> taskViewModel.updateTaskStatus(task, TaskStatus.COMPLETED));
        binding.buttonCancelTask.setOnClickListener(v -> taskViewModel.updateTaskStatus(task, TaskStatus.CANCELLED));

        binding.buttonPauseTask.setOnClickListener(v -> {
            TaskStatus newStatus = (task.getStatus() == TaskStatus.PAUSED) ? TaskStatus.ACTIVE : TaskStatus.PAUSED;
            taskViewModel.updateTaskStatus(task, newStatus);
        });

        // TODO: Implementirati listenere za Edit i Delete dugmad, poštujući pravila.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
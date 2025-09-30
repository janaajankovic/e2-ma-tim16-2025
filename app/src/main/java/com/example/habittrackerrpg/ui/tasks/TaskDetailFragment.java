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
    private Task currentTask;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentTask = (Task) getArguments().getSerializable("task");
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

        if (currentTask != null) {
            setupListeners();
            taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
                if (categories != null) {
                    populateUI();
                }
            });
        }
    }

    private void populateUI() {
        binding.textTaskDetailName.setText(currentTask.getName());
        binding.textTaskDetailDescription.setText(currentTask.getDescription());

        populateScheduleInfo();
        populateCategoryInfo();

        binding.textDifficultyInfo.setText("Difficulty: " + currentTask.getDifficulty().name());
        binding.textImportanceInfo.setText("Importance: " + currentTask.getImportance().name());
        binding.textXpValueInfo.setText("Value: +" + currentTask.getXpValue() + " XP");

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            for (Task updatedTask : tasks) {
                if (updatedTask.getId().equals(currentTask.getId())) {
                    currentTask = updatedTask;
                    updateStatusUI(currentTask.getStatus());
                    break;
                }
            }
        });
    }

    private void populateScheduleInfo() {
        if (currentTask.isRecurring()) {
            String unit = currentTask.getRecurrenceUnit();
            int interval = currentTask.getRecurrenceInterval();
            String scheduleText = "Repeats every " + interval + " " + unit + " at " + timeFormat.format(currentTask.getRecurrenceStartDate());
            binding.textScheduleInfo.setText(scheduleText);
        } else {
            binding.textScheduleInfo.setText("Due: " + dateTimeFormat.format(currentTask.getDueDate()));
        }
    }

    private void populateCategoryInfo() {
        Category category = findCategoryById(currentTask.getCategoryId());
        if (category != null) {
            binding.textCategoryInfo.setText("Category: " + category.getName());

            int categoryColor = Color.parseColor(category.getColor());
            binding.textCategoryInfo.setCompoundDrawableTintList(ColorStateList.valueOf(categoryColor));
        } else {
            binding.textCategoryInfo.setText("Category: Not found");
            binding.textCategoryInfo.setCompoundDrawableTintList(ColorStateList.valueOf(Color.GRAY));
        }
    }

    private void updateStatusUI(TaskStatus status) {
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
            default:
                binding.chipTaskStatus.setChipBackgroundColorResource(R.color.action_color_pause);
                break;
        }
        boolean isActionable = status == TaskStatus.ACTIVE || status == TaskStatus.PAUSED;
        binding.actionsContainer.setVisibility(isActionable ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        binding.buttonMarkDone.setOnClickListener(v -> taskViewModel.updateTaskStatus(currentTask, TaskStatus.COMPLETED));
        binding.buttonCancelTask.setOnClickListener(v -> taskViewModel.updateTaskStatus(currentTask, TaskStatus.CANCELLED));
        binding.buttonPauseTask.setOnClickListener(v -> {
            TaskStatus newStatus = (currentTask.getStatus() == TaskStatus.PAUSED) ? TaskStatus.ACTIVE : TaskStatus.PAUSED;
            taskViewModel.updateTaskStatus(currentTask, newStatus);
        });
        // TODO: Implement listeners for Edit and Delete buttons
    }

    private Category findCategoryById(String categoryId) {
        if (taskViewModel.getCategories().getValue() != null && categoryId != null) {
            for (Category category : taskViewModel.getCategories().getValue()) {
                if (categoryId.equals(category.getId())) {
                    return category;
                }
            }
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
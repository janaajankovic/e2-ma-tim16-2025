package com.example.habittrackerrpg.ui.tasks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DayTasksAdapter extends RecyclerView.Adapter<DayTasksAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onCompleteClick(Task task);
        void onCancelClick(Task task);
        void onPauseClick(Task task);
        void onTaskClick(Task task);
    }

    private final List<Task> tasks;
    private final Map<String, Category> categoriesById;
    private final OnTaskActionListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public DayTasksAdapter(List<Task> tasks, Map<String, Category> categoriesById, OnTaskActionListener listener) {
        this.tasks = new ArrayList<>(tasks);
        this.categoriesById = new HashMap<>(categoriesById);
        this.listener = listener;
    }

    public void updateData(List<Task> newTasks, Map<String, Category> newCategories) {
        this.tasks.clear();
        this.tasks.addAll(newTasks);
        this.categoriesById.clear();
        this.categoriesById.putAll(newCategories);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_day_list, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskName, taskTime;
        private final View categoryColorBar;
        private final View taskInfoContainer;
        private final Chip taskStatusChip;
        private final View actionsContainer, divider;
        private final MaterialButton completeButton, cancelButton, pauseButton, activateButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskInfoContainer = itemView.findViewById(R.id.task_info_container);
            taskName = itemView.findViewById(R.id.text_task_name);
            taskTime = itemView.findViewById(R.id.text_task_time);
            categoryColorBar = itemView.findViewById(R.id.view_category_color_bar);
            taskStatusChip = itemView.findViewById(R.id.chip_task_status);
            actionsContainer = itemView.findViewById(R.id.actions_container);
            divider = itemView.findViewById(R.id.divider);
            completeButton = itemView.findViewById(R.id.button_complete_task);
            cancelButton = itemView.findViewById(R.id.button_cancel_task);
            pauseButton = itemView.findViewById(R.id.button_pause_task);
            activateButton = itemView.findViewById(R.id.button_activate_task);
        }

        public void bind(final Task task) {
            taskName.setText(task.getName());

            if (task.isRecurring() && task.getRecurrenceStartDate() != null) {
                taskTime.setText(timeFormat.format(task.getRecurrenceStartDate()));
            } else if (task.getDueDate() != null) {
                taskTime.setText(timeFormat.format(task.getDueDate()));
            }

            Category category = categoriesById.get(task.getCategoryId());
            if (category != null) {
                categoryColorBar.setBackgroundColor(Color.parseColor(category.getColor()));
            }

            updateStatusUI(task);

            completeButton.setOnClickListener(v -> listener.onCompleteClick(task));
            cancelButton.setOnClickListener(v -> listener.onCancelClick(task));
            pauseButton.setOnClickListener(v -> listener.onPauseClick(task));
            activateButton.setOnClickListener(v -> listener.onPauseClick(task));

            taskInfoContainer.setOnClickListener(v -> listener.onTaskClick(task));
        }

        private void updateStatusUI(Task task) {
            taskStatusChip.setText(task.getStatus().name());

            boolean isActionable = task.getStatus() == TaskStatus.ACTIVE || task.getStatus() == TaskStatus.PAUSED;
            actionsContainer.setVisibility(isActionable ? View.VISIBLE : View.GONE);
            divider.setVisibility(isActionable ? View.VISIBLE : View.GONE);

            switch (task.getStatus()) {
                case COMPLETED:
                    taskStatusChip.setChipBackgroundColorResource(R.color.action_color_done);
                    break;
                case CANCELLED:
                    taskStatusChip.setChipBackgroundColorResource(R.color.icon_tint_default);
                    break;
                case PAUSED:
                    taskStatusChip.setChipBackgroundColorResource(R.color.icon_color_on_action);
                    break;
                case UNCOMPLETED:
                    taskStatusChip.setChipBackgroundColorResource(R.color.action_color_cancel);
                    break;
                default:
                    taskStatusChip.setChipBackgroundColorResource(R.color.action_color_pause);
                    break;
            }

            if (isActionable) {
                if (task.getStatus() == TaskStatus.PAUSED) {
                    pauseButton.setVisibility(View.GONE);
                    activateButton.setVisibility(View.VISIBLE);
                } else {
                    pauseButton.setVisibility(task.isRecurring() ? View.VISIBLE : View.GONE);
                    activateButton.setVisibility(View.GONE);
                }
                completeButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
            } else {
                completeButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                activateButton.setVisibility(View.GONE);
            }
        }
    }
}
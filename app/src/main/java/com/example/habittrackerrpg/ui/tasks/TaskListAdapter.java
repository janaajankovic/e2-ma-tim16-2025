package com.example.habittrackerrpg.ui.tasks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Locale;

public class TaskListAdapter extends ListAdapter<Task, TaskListAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onCompleteClick(Task task);
        void onCancelClick(Task task);
        void onPauseClick(Task task);
        void onTaskClick(Task task);
    }

    private final Map<String, Category> categoriesById;
    private final OnTaskActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public TaskListAdapter(Map<String, Category> categoriesById, OnTaskActionListener listener) {
        super(DIFF_CALLBACK);
        this.categoriesById = categoriesById;
        this.listener = listener;
    }

    public void setCategories(Map<String, Category> newCategories) {
        this.categoriesById.clear();
        this.categoriesById.putAll(newCategories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_main_list, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task);
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            if (oldItem.isRecurring() && newItem.isRecurring()) {
                return oldItem.getId().equals(newItem.getId()) &&
                        Objects.equals(oldItem.getDueDate(), newItem.getDueDate());
            }
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.equals(newItem);
        }
    };

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskName, taskDueDate;
        private final View categoryColorBar;
        private final Chip taskStatusChip;
        private final MaterialButton completeButton, cancelButton, pauseButton;
        private final View actionsContainer;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.text_task_name);
            taskDueDate = itemView.findViewById(R.id.text_task_due_date);
            categoryColorBar = itemView.findViewById(R.id.view_category_color_bar);
            taskStatusChip = itemView.findViewById(R.id.chip_task_status);
            completeButton = itemView.findViewById(R.id.button_complete_task);
            cancelButton = itemView.findViewById(R.id.button_cancel_task);
            pauseButton = itemView.findViewById(R.id.button_pause_task);
            actionsContainer = itemView.findViewById(R.id.actions_container);
        }

        public void bind(final Task task) {
            taskName.setText(task.getName());

            if (task.getDueDate() != null) {
                taskDueDate.setText("Due: " + dateFormat.format(task.getDueDate()));
            } else {
                taskDueDate.setText("No due date");
            }

            Category category = categoriesById.get(task.getCategoryId());
            if (category != null) {
                categoryColorBar.setBackgroundColor(Color.parseColor(category.getColor()));
            }

            updateStatusUI(task);

            itemView.setOnClickListener(v -> listener.onTaskClick(task));
            completeButton.setOnClickListener(v -> listener.onCompleteClick(task));
            cancelButton.setOnClickListener(v -> listener.onCancelClick(task));
            pauseButton.setOnClickListener(v -> listener.onPauseClick(task));
        }

        private void updateStatusUI(Task task) {
            taskStatusChip.setText(task.getStatus().name());

            switch (task.getStatus()) {
                case COMPLETED:
                    taskStatusChip.setChipBackgroundColorResource(R.color.action_color_done); break;
                case CANCELLED:
                    taskStatusChip.setChipBackgroundColorResource(R.color.icon_tint_default); break;
                case PAUSED:
                    taskStatusChip.setChipBackgroundColorResource(R.color.icon_color_on_action); break;
                case UNCOMPLETED:
                    taskStatusChip.setChipBackgroundColorResource(R.color.action_color_cancel); break;
                default:
                    taskStatusChip.setChipBackgroundColorResource(R.color.action_color_pause); break;
            }

            if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED || task.getStatus() == TaskStatus.UNCOMPLETED) {
                actionsContainer.setVisibility(View.GONE);
                return;
            }

            actionsContainer.setVisibility(View.VISIBLE);

            if (task.getStatus() == TaskStatus.ACTIVE) {
                completeButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);

                if (task.isRecurring()) {
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setText("Pause");
                    pauseButton.setIconResource(R.drawable.ic_pause_circle);
                } else {
                    pauseButton.setVisibility(View.GONE);
                }

            } else if (task.getStatus() == TaskStatus.PAUSED) {
                completeButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);

                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setText("Activate");
                pauseButton.setIconResource(R.drawable.ic_play_circle);
            }
        }
    }
}
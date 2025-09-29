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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DayTasksAdapter extends RecyclerView.Adapter<DayTasksAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    private final List<Task> tasks;
    private final Map<String, Category> categoriesById;
    private final OnTaskClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public DayTasksAdapter(List<Task> tasks, Map<String, Category> categoriesById, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.categoriesById = categoriesById;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // FIX: Use the simple layout, not the interactive one
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
        private final TextView taskName;
        private final TextView taskTime;
        private final View categoryColorBar;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.text_task_name);
            taskTime = itemView.findViewById(R.id.text_task_time);
            categoryColorBar = itemView.findViewById(R.id.view_category_color_bar);
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

            itemView.setOnClickListener(v -> listener.onTaskClick(task));
        }
    }
}
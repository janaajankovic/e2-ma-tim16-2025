package com.example.habittrackerrpg.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.databinding.BottomSheetDayTasksBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DayTasksBottomSheetFragment extends BottomSheetDialogFragment implements DayTasksAdapter.OnTaskClickListener  {

    private BottomSheetDayTasksBinding binding;
    private List<Task> tasks;

    private Map<String, Category> categoriesById;
    private static final String ARG_TASKS = "tasks";
    private static final String ARG_DATE_TITLE = "date_title";

    public interface TaskSelectionListener {
        void onTaskSelected(Task task);
    }

    private TaskSelectionListener selectionListener;

    public void setTaskSelectionListener(TaskSelectionListener listener) {
        this.selectionListener = listener;
    }

    public static DayTasksBottomSheetFragment newInstance(List<Task> tasks, String dateTitle, HashMap<String, Category> categories) {
        DayTasksBottomSheetFragment fragment = new DayTasksBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASKS, (Serializable) tasks);
        args.putString(ARG_DATE_TITLE, dateTitle);
        args.putSerializable("categories", categories);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tasks = (List<Task>) getArguments().getSerializable(ARG_TASKS);
            categoriesById = (Map<String, Category>) getArguments().getSerializable("categories");
        }
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

        binding.textBottomSheetTitle.setText(getArguments().getString(ARG_DATE_TITLE));

        DayTasksAdapter adapter = new DayTasksAdapter(tasks, categoriesById, this);
        binding.recyclerViewDayTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewDayTasks.setAdapter(adapter);
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
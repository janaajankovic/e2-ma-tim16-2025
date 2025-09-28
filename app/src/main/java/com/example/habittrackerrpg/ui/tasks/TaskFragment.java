package com.example.habittrackerrpg.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.habittrackerrpg.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TaskFragment extends Fragment {

    public TaskFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fabAddTask = view.findViewById(R.id.fab_add_task);
        Button btnManageCategories = view.findViewById(R.id.button_manage_categories);

        fabAddTask.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_tasks_to_createTaskFragment);
        });

        btnManageCategories.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_tasks_to_manageCategoriesFragment);
        });
    }
}
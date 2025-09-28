package com.example.habittrackerrpg.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Task;

public class CreateTaskFragment extends Fragment {

    private CheckBox recurringCheckbox;
    private LinearLayout recurringOptionsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recurringCheckbox = view.findViewById(R.id.checkbox_recurring);
        recurringOptionsContainer = view.findViewById(R.id.recurring_options_container);

        recurringCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            recurringOptionsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        Button saveButton = view.findViewById(R.id.button_save_task);
        saveButton.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {

        Task newTask = new Task();
        Toast.makeText(getContext(), "Zadatak sačuvan!", Toast.LENGTH_SHORT).show();
    }

}
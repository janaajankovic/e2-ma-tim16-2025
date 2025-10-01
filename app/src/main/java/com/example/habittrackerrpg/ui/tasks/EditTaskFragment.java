package com.example.habittrackerrpg.ui.tasks;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskDifficulty;
import com.example.habittrackerrpg.data.model.TaskImportance;
import com.example.habittrackerrpg.databinding.FragmentEditTaskBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditTaskFragment extends Fragment {

    private FragmentEditTaskBinding binding;
    private TaskViewModel taskViewModel;
    private Task originalTask;
    private final Calendar selectedTimeCalendar = Calendar.getInstance();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            originalTask = (Task) getArguments().getSerializable("taskToEdit");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        if (originalTask == null) {
            Toast.makeText(getContext(), "Error: Task not found.", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        populateUi();
        setupListeners();
    }

    private void populateUi() {
        binding.editTextTaskName.setText(originalTask.getName());
        binding.editTextTaskDescription.setText(originalTask.getDescription());

        taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categories.stream()
                        .filter(c -> c.getId().equals(originalTask.getCategoryId()))
                        .findFirst();
            }
        });

        Date taskTime = originalTask.isRecurring() ? originalTask.getRecurrenceStartDate() : originalTask.getDueDate();
        if (taskTime != null) {
            selectedTimeCalendar.setTime(taskTime);
            binding.textViewSelectedTime.setText(timeFormat.format(selectedTimeCalendar.getTime()));
        }

        switch (originalTask.getDifficulty()) {
            case VERY_EASY: binding.radioGroupDifficulty.check(R.id.radio_very_easy); break;
            case EASY: binding.radioGroupDifficulty.check(R.id.radio_easy); break;
            case HARD: binding.radioGroupDifficulty.check(R.id.radio_hard); break;
            case EXTREME: binding.radioGroupDifficulty.check(R.id.radio_extreme); break;
        }
        switch (originalTask.getImportance()) {
            case NORMAL: binding.radioGroupImportance.check(R.id.radio_normal); break;
            case IMPORTANT: binding.radioGroupImportance.check(R.id.radio_important); break;
            case EXTREMELY_IMPORTANT: binding.radioGroupImportance.check(R.id.radio_extremely_important); break;
            case SPECIAL: binding.radioGroupImportance.check(R.id.radio_special); break;
        }
    }

    private void setupListeners() {
        binding.buttonSelectTime.setOnClickListener(v -> showTimePickerDialog());
        binding.buttonSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void showTimePickerDialog() {
        int hour = selectedTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = selectedTimeCalendar.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minuteOfHour) -> {
            selectedTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedTimeCalendar.set(Calendar.MINUTE, minuteOfHour);

            binding.textViewSelectedTime.setText(timeFormat.format(selectedTimeCalendar.getTime()));
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                timeSetListener,
                hour,
                minute,
                DateFormat.is24HourFormat(requireContext())
        );

        timePickerDialog.show();
    }

    private void saveChanges() {
        String name = binding.editTextTaskName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Task name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Task editedData = new Task(originalTask);
        editedData.setName(name);
        editedData.setDescription(binding.editTextTaskDescription.getText().toString().trim());
        editedData.setDifficulty(getSelectedDifficulty());
        editedData.setImportance(getSelectedImportance());

        if (editedData.isRecurring()) {
            Date oldStartDate = editedData.getRecurrenceStartDate();
            Calendar newStartDateCal = Calendar.getInstance();
            if (oldStartDate != null) newStartDateCal.setTime(oldStartDate);
            newStartDateCal.set(Calendar.HOUR_OF_DAY, selectedTimeCalendar.get(Calendar.HOUR_OF_DAY));
            newStartDateCal.set(Calendar.MINUTE, selectedTimeCalendar.get(Calendar.MINUTE));
            editedData.setRecurrenceStartDate(newStartDateCal.getTime());
        } else {
            Date oldDueDate = editedData.getDueDate();
            Calendar newDueDateCal = Calendar.getInstance();
            if (oldDueDate != null) newDueDateCal.setTime(oldDueDate);
            newDueDateCal.set(Calendar.HOUR_OF_DAY, selectedTimeCalendar.get(Calendar.HOUR_OF_DAY));
            newDueDateCal.set(Calendar.MINUTE, selectedTimeCalendar.get(Calendar.MINUTE));
            editedData.setDueDate(newDueDateCal.getTime());
        }

        taskViewModel.editTask(originalTask, editedData);
        NavHostFragment.findNavController(this).popBackStack();
    }

    private TaskDifficulty getSelectedDifficulty() {
        int selectedId = binding.radioGroupDifficulty.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_very_easy) {
            return TaskDifficulty.VERY_EASY;
        } else if (selectedId == R.id.radio_easy) {
            return TaskDifficulty.EASY;
        } else if (selectedId == R.id.radio_hard) {
            return TaskDifficulty.HARD;
        } else if (selectedId == R.id.radio_extreme) {
            return TaskDifficulty.EXTREME;
        }
        return TaskDifficulty.EASY;
    }

    private TaskImportance getSelectedImportance() {
        int selectedId = binding.radioGroupImportance.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_normal) {
            return TaskImportance.NORMAL;
        } else if (selectedId == R.id.radio_important) {
            return TaskImportance.IMPORTANT;
        } else if (selectedId == R.id.radio_extremely_important) {
            return TaskImportance.EXTREMELY_IMPORTANT;
        } else if (selectedId == R.id.radio_special) {
            return TaskImportance.SPECIAL;
        }
        return TaskImportance.NORMAL;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.habittrackerrpg.ui.tasks;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskDifficulty;
import com.example.habittrackerrpg.data.model.TaskImportance;
import com.example.habittrackerrpg.databinding.FragmentCreateTaskBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateTaskFragment extends Fragment {

    private FragmentCreateTaskBinding binding;
    private TaskViewModel taskViewModel;
    private List<Category> categoryList;
    private Calendar dueDate = Calendar.getInstance();
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private void setupDatePickers() {
        binding.buttonDueDate.setOnClickListener(v -> showDateTimePickerDialog(dueDate, binding.textViewDueDate));
        binding.buttonStartDate.setOnClickListener(v -> showDateTimePickerDialog(startDate, binding.textViewStartDate));
        binding.buttonEndDate.setOnClickListener(v -> showDatePickerDialog(endDate, binding.textViewEndDate)); // Calls date-only picker
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        setupCategorySpinner();
        setupRecurringOptions();
        setupDatePickers();

        binding.buttonSaveTask.setOnClickListener(v -> saveTask());

        taskViewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                if (message.equals("Task created successfully!")) {
                    NavHostFragment.findNavController(this).popBackStack();
                }
            }
        });
    }

    private void setupRecurringOptions() {
        binding.checkboxRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.recurringOptionsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            binding.oneTimeTaskContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });
    }

    private void showDateTimePickerDialog(final Calendar calendar, final TextView textViewToUpdate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                textViewToUpdate.setText(dateTimeFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true); // true za 24h format

            timePickerDialog.show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void showDatePickerDialog(final Calendar calendar, final TextView textViewToUpdate) {
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            textViewToUpdate.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupCategorySpinner() {
        taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            this.categoryList = categories;
            ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(requireContext(), android.R.layout.simple_spinner_item, categories) {
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView label = (TextView) super.getView(position, convertView, parent);
                    label.setText(categories.get(position).getName());
                    return label;
                }
                @Override
                public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView label = (TextView) super.getDropDownView(position, convertView, parent);
                    label.setText(categories.get(position).getName());
                    return label;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerCategory.setAdapter(adapter);
        });
    }

    private void saveTask() {
        Task task = new Task();
        task.setName(binding.editTextTaskName.getText().toString());
        task.setDescription(binding.editTextTaskDescription.getText().toString());

        task.setAwardedXp(0);

        if (binding.spinnerCategory.getSelectedItem() != null) {
            Category selectedCategory = (Category) binding.spinnerCategory.getSelectedItem();
            task.setCategoryId(selectedCategory.getId());
        }

        task.setDifficulty(getSelectedDifficulty());
        task.setImportance(getSelectedImportance());

        boolean isRecurring = binding.checkboxRecurring.isChecked();
        task.setRecurring(isRecurring);
        if (isRecurring) {
            try {
                int interval = Integer.parseInt(binding.editTextInterval.getText().toString());
                task.setRecurrenceInterval(interval);
            } catch (NumberFormatException e) {
                task.setRecurrenceInterval(1);
            }
            task.setRecurrenceUnit(binding.spinnerRecurrenceUnit.getSelectedItem().toString());
            task.setRecurrenceStartDate(startDate.getTime());
            task.setRecurrenceEndDate(endDate.getTime());
        } else {
            task.setDueDate(dueDate.getTime());
        }

        taskViewModel.addTask(task);
    }

    private TaskDifficulty getSelectedDifficulty() {
        int selectedId = binding.radioGroupDifficulty.getCheckedRadioButtonId();
        if (selectedId == binding.radioVeryEasy.getId()) return TaskDifficulty.VERY_EASY;
        if (selectedId == binding.radioEasy.getId()) return TaskDifficulty.EASY;
        if (selectedId == binding.radioHard.getId()) return TaskDifficulty.HARD;
        if (selectedId == binding.radioExtreme.getId()) return TaskDifficulty.EXTREME;
        return null;
    }

    private TaskImportance getSelectedImportance() {
        int selectedId = binding.radioGroupImportance.getCheckedRadioButtonId();
        if (selectedId == binding.radioNormal.getId()) return TaskImportance.NORMAL;
        if (selectedId == binding.radioImportant.getId()) return TaskImportance.IMPORTANT;
        if (selectedId == binding.radioExtremelyImportant.getId()) return TaskImportance.EXTREMELY_IMPORTANT;
        if (selectedId == binding.radioSpecial.getId()) return TaskImportance.SPECIAL;
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.habittrackerrpg.ui.tasks;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.logic.GenerateTaskOccurrencesUseCase;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarViewFragment extends Fragment implements DayTasksBottomSheetFragment.TaskSelectionListener {

    private TaskViewModel taskViewModel;
    private CalendarView calendarView;
    private TextView monthTitleTextView;
    private Map<LocalDate, List<Task>> tasksByDate = new HashMap<>();
    private Map<String, Category> categoriesById = new HashMap<>();
    private LocalDate selectedDate = LocalDate.now();
    private GenerateTaskOccurrencesUseCase generateOccurrencesUseCase = new GenerateTaskOccurrencesUseCase();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        calendarView = view.findViewById(R.id.calendar_view);
        monthTitleTextView = view.findViewById(R.id.text_month_title);

        setupObservers();
        setupCalendar();
    }

    private void setupObservers() {
        taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoriesById = categories.stream()
                        .filter(c -> c.getId() != null)
                        .collect(Collectors.toMap(Category::getId, c -> c));
                refreshCalendarData();
            }
        });

        taskViewModel.getTaskRules().observe(getViewLifecycleOwner(), rules -> refreshCalendarData());
        taskViewModel.getTaskInstances().observe(getViewLifecycleOwner(), instances -> refreshCalendarData());
    }

    private void refreshCalendarData() {
        List<Task> taskRules = taskViewModel.getTaskRules().getValue();
        List<TaskInstance> taskInstances = taskViewModel.getTaskInstances().getValue();

        if (taskRules == null || taskInstances == null) {
            return;
        }

        tasksByDate.clear();

        Map<String, TaskInstance> instanceMap = taskInstances.stream()
                .collect(Collectors.toMap(
                        instance -> instance.getOriginalTaskId() + "_" + instance.getInstanceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                        instance -> instance,
                        (a, b) -> b
                ));

        YearMonth currentMonth = YearMonth.now();
        LocalDate rangeStart = currentMonth.minusMonths(12).atDay(1);
        LocalDate rangeEnd = currentMonth.plusMonths(12).atEndOfMonth();

        for (Task rule : taskRules) {
            List<LocalDate> occurrences = generateOccurrencesUseCase.execute(rule, rangeStart, rangeEnd);

            for (LocalDate occurrenceDate : occurrences) {
                String instanceKey = rule.getId() + "_" + occurrenceDate.toString();
                TaskInstance instance = instanceMap.get(instanceKey);

                Task virtualTask = new Task(rule);
                Date dateForDisplay = Date.from(occurrenceDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                virtualTask.setDueDate(dateForDisplay);

                if (instance != null) {
                    virtualTask.setStatus(instance.getStatus());
                } else {
                    virtualTask.setStatus(rule.getStatus());
                }

                tasksByDate.computeIfAbsent(occurrenceDate, k -> new ArrayList<>()).add(virtualTask);

            }
        }

        calendarView.notifyCalendarChanged();
    }


    private void setupCalendar() {
        YearMonth currentMonth = YearMonth.now();
        calendarView.setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), java.time.DayOfWeek.MONDAY);
        calendarView.scrollToMonth(currentMonth);

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                DayViewContainer container = new DayViewContainer(view);
                container.getView().setOnClickListener(v -> onDayClicked(container.day));
                return container;
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay day) {
                container.day = day;
                container.textView.setText(String.valueOf(day.getDate().getDayOfMonth()));
                container.dotsContainer.removeAllViews();

                List<Task> dayTasks = tasksByDate.get(day.getDate());
                if (dayTasks != null && !dayTasks.isEmpty()) {
                    dayTasks.stream()
                            .map(Task::getCategoryId)
                            .distinct()
                            .limit(4)
                            .forEach(categoryId -> {
                                Category category = categoriesById.get(categoryId);
                                if (category != null) {
                                    View dot = new View(getContext());
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
                                    params.setMargins(4, 0, 4, 0);
                                    dot.setLayoutParams(params);
                                    dot.setBackgroundResource(R.drawable.color_circle);
                                    dot.getBackground().mutate().setTint(Color.parseColor(category.getColor()));
                                    container.dotsContainer.addView(dot);
                                }
                            });
                }
            }
        });

        calendarView.setMonthScrollListener(calendarMonth -> {
            String title = calendarMonth.getYearMonth().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                    + " " + calendarMonth.getYearMonth().getYear();
            monthTitleTextView.setText(title);
            return null;
        });
    }

    @Override
    public void onTaskSelected(Task task) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("task", task);
        NavHostFragment.findNavController(this).navigate(R.id.action_calendarViewFragment_to_taskDetailFragment, bundle);
    }

    private void onDayClicked(CalendarDay day) {
        LocalDate clickedDate = day.getDate();
        if (!clickedDate.equals(selectedDate)) {
            LocalDate oldDate = selectedDate;
            selectedDate = clickedDate;
            calendarView.notifyDateChanged(clickedDate);
            if (oldDate != null) {
                calendarView.notifyDateChanged(oldDate);
            }
        }

        String title = "Tasks for: " + clickedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        DayTasksBottomSheetFragment bottomSheet = DayTasksBottomSheetFragment.newInstance(clickedDate, title);
        bottomSheet.setTaskSelectionListener(this);
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }

    private static class DayViewContainer extends ViewContainer {
        TextView textView;
        LinearLayout dotsContainer;
        CalendarDay day;

        public DayViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.text_day);
            dotsContainer = view.findViewById(R.id.dots_container);
        }
    }
}
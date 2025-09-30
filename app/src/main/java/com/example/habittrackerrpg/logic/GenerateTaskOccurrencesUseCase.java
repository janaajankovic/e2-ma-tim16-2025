package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenerateTaskOccurrencesUseCase {

    public List<LocalDate> execute(Task task, LocalDate rangeStart, LocalDate rangeEnd) {
        List<LocalDate> occurrences = new ArrayList<>();

        if (!task.isRecurring()) {
            if (task.getDueDate() != null) {
                LocalDate dueDate = task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (!dueDate.isBefore(rangeStart) && !dueDate.isAfter(rangeEnd)) {
                    occurrences.add(dueDate);
                }
            }
        } else {
            if (task.getRecurrenceStartDate() == null || task.getRecurrenceEndDate() == null) {
                return occurrences;
            }

            LocalDate startDate = task.getRecurrenceStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = task.getRecurrenceEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                if (!currentDate.isBefore(rangeStart) && !currentDate.isAfter(rangeEnd)) {
                    occurrences.add(currentDate);
                }

                if (Objects.equals(task.getRecurrenceUnit(), "Day(s)")) {
                    currentDate = currentDate.plusDays(task.getRecurrenceInterval());
                } else if (Objects.equals(task.getRecurrenceUnit(), "Week(s)")) {
                    currentDate = currentDate.plusWeeks(task.getRecurrenceInterval());
                } else {
                    break;
                }
            }
        }
        return occurrences;
    }
}
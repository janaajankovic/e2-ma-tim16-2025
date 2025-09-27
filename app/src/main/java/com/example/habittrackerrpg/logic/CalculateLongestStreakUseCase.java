package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateLongestStreakUseCase {
    public int execute(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return 0;

        Map<LocalDate, Boolean> dailyCompletionStatus = new HashMap<>();

        // Prolazimo kroz sve zadatke i za svaki dan beležimo da li su svi zadaci tog dana završeni
        Map<LocalDate, List<Task>> tasksByDay = new HashMap<>();
        for (Task task : tasks) {
            LocalDate date = task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            tasksByDay.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
        }

        for (Map.Entry<LocalDate, List<Task>> entry : tasksByDay.entrySet()) {
            boolean allTasksCompleted = true;
            for (Task task : entry.getValue()) {
                if (task.getStatus() != TaskStatus.COMPLETED) {
                    allTasksCompleted = false;
                    break;
                }
            }
            dailyCompletionStatus.put(entry.getKey(), allTasksCompleted);
        }

        List<LocalDate> sortedDates = new ArrayList<>(dailyCompletionStatus.keySet());
        Collections.sort(sortedDates);

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate previousDate = null;

        for (LocalDate date : sortedDates) {
            if (dailyCompletionStatus.get(date)) { // Ako su svi zadaci za taj dan završeni
                if (previousDate == null || date.isEqual(previousDate.plusDays(1))) {
                    currentStreak++;
                } else {
                    currentStreak = 1;
                }
            } else { // Ako bar jedan zadatak nije završen, niz se prekida
                currentStreak = 0;
            }

            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
            previousDate = date;
        }

        return longestStreak;
    }
}
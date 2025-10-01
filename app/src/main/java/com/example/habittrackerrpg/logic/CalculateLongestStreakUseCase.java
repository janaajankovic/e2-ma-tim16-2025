package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalculateLongestStreakUseCase {
    public int execute(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }

        Map<LocalDate, List<Task>> tasksByDay = new HashMap<>();
        for (Task task : tasks) {
            if (task.getDueDate() != null) {
                LocalDate date = task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                tasksByDay.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
            }
        }

        Set<LocalDate> streakBreakerDays = new HashSet<>();
        Set<LocalDate> completedDays = new HashSet<>();

        for (Map.Entry<LocalDate, List<Task>> entry : tasksByDay.entrySet()) {
            LocalDate day = entry.getKey();
            List<Task> dayTasks = entry.getValue();

            boolean hasCompletedTask = false;
            boolean hasUncompletedTask = false;

            for (Task task : dayTasks) {
                if (task.getStatus() == TaskStatus.COMPLETED) {
                    hasCompletedTask = true;
                } else if (task.getStatus() == TaskStatus.UNCOMPLETED) { // Pretpostavka da postoji UNCOMPLETED status
                    hasUncompletedTask = true;
                }
            }

            if (hasCompletedTask) {
                completedDays.add(day);
            }
            if (hasUncompletedTask && !hasCompletedTask) {
                // Dan prekida niz samo ako ima neurađen zadatak, A NEMA nijedan urađen
                streakBreakerDays.add(day);
            }
        }

        if (completedDays.isEmpty()) {
            return 0;
        }

        List<LocalDate> sortedCompletedDays = new ArrayList<>(completedDays);
        Collections.sort(sortedCompletedDays);

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate lastDayInStreak = null;

        for (LocalDate day : sortedCompletedDays) {
            if (lastDayInStreak == null) {
                // Početak prvog niza
                currentStreak = 1;
            } else {
                // Proveravamo da li između ovog i prethodnog dana u nizu postoji dan koji prekida niz
                boolean isStreakBroken = false;
                LocalDate tempDate = lastDayInStreak.plusDays(1);
                while (tempDate.isBefore(day)) {
                    if (streakBreakerDays.contains(tempDate)) {
                        isStreakBroken = true;
                        break;
                    }
                    tempDate = tempDate.plusDays(1);
                }

                if (isStreakBroken) {
                    currentStreak = 1; // Niz je prekinut, počni novi
                } else {
                    currentStreak++; // Nastavi niz
                }
            }

            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
            lastDayInStreak = day;
        }
        return longestStreak;
    }
}
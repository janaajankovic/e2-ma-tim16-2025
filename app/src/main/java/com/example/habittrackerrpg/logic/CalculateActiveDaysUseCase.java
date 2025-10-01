package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalculateActiveDaysUseCase {

    public int execute(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }

        Set<LocalDate> activeDays = new HashSet<>();
        for (Task task : tasks) {
            // Dodajemo dan kada je zadatak kreiran
            if (task.getCreatedAt() != null) {
                LocalDate createdAtDate = Instant.ofEpochMilli(task.getCreatedAt().getTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                activeDays.add(createdAtDate);
            }

            if (task.getCompletedAt() != null) {
                LocalDate completedAtDate = Instant.ofEpochMilli(task.getCompletedAt().getTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                activeDays.add(completedAtDate);
            }
        }

        return activeDays.size();
    }
}
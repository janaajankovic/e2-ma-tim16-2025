package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateDailyXpUseCase {

    private final CalculateTaskXpUseCase xpCalculator;

    public CalculateDailyXpUseCase() {
        this.xpCalculator = new CalculateTaskXpUseCase();
    }

    public Map<LocalDate, List<Integer>> getDailyXpMap(List<Task> tasks, int userLevel) {
        Map<LocalDate, List<Integer>> dailyXpValues = new HashMap<>();
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                LocalDate date = task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int scaledXp = xpCalculator.execute(task, userLevel);
                dailyXpValues.computeIfAbsent(date, k -> new ArrayList<>()).add(scaledXp);
            }
        }
        return dailyXpValues;
    }
}
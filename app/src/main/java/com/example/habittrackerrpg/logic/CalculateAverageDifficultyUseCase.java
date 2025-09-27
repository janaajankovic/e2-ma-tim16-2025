package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.data.model.User;
import com.github.mikephil.charting.data.Entry;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateAverageDifficultyUseCase {

    public static class Result {
        public final List<Entry> chartEntries;
        public final String summaryDescription;

        public Result(List<Entry> chartEntries, String summaryDescription) {
            this.chartEntries = chartEntries;
            this.summaryDescription = summaryDescription;
        }
    }

    public Result execute(List<Task> tasks, User user, CalculateTaskXpUseCase xpCalculator) {
        List<Task> completedTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                completedTasks.add(task);
            }
        }

        if (completedTasks.isEmpty() || user == null) {
            return new Result(new ArrayList<>(), "No completed tasks yet.");
        }

        float totalXp = 0;
        for (Task task : completedTasks) {
            totalXp += xpCalculator.execute(task, user.getLevel());
        }
        float overallAverageXp = totalXp / completedTasks.size();
        String description;
        if (overallAverageXp <= 4) description = "You mostly solve VERY EASY tasks.";
        else if (overallAverageXp <= 10) description = "You mostly solve EASY tasks.";
        else if (overallAverageXp <= 20) description = "You mostly solve HARD tasks.";
        else description = "You mostly solve EXTREME tasks.";

        Map<LocalDate, List<Integer>> dailyXpValues = new HashMap<>();
        for (Task task : completedTasks) {
            if (task.getDueDate() != null) {
                LocalDate date = task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int xp = xpCalculator.execute(task, user.getLevel());
                dailyXpValues.computeIfAbsent(date, k -> new ArrayList<>()).add(xp);
            }
        }
        Map<LocalDate, Float> dailyAverageXp = new HashMap<>();
        for(Map.Entry<LocalDate, List<Integer>> entry : dailyXpValues.entrySet()){
            float sum = 0;
            for(Integer xp : entry.getValue()){ sum += xp; }
            dailyAverageXp.put(entry.getKey(), sum / entry.getValue().size());
        }
        List<Entry> chartEntries = new ArrayList<>();
        List<LocalDate> sortedDates = new ArrayList<>(dailyAverageXp.keySet());
        Collections.sort(sortedDates);
        for (LocalDate date : sortedDates) {
            chartEntries.add(new Entry(date.toEpochDay(), dailyAverageXp.get(date)));
        }

        return new Result(chartEntries, description);
    }
}
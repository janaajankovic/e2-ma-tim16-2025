package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.github.mikephil.charting.data.Entry;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateDailyXpUseCase {

    // Vraća mapu gde je ključ datum, a vrednost je lista XP poena za taj dan
    public Map<LocalDate, List<Integer>> getDailyXpMap(List<Task> tasks) {
        Map<LocalDate, List<Integer>> dailyXpValues = new HashMap<>();
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                LocalDate date = task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                dailyXpValues.computeIfAbsent(date, k -> new ArrayList<>()).add(task.calculateXp());
            }
        }
        return dailyXpValues;
    }
}
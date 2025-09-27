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

public class CalculateAverageTaskDifficultyUseCase {

    public List<Entry> execute(List<Task> tasks) {
        Map<LocalDate, List<Integer>> dailyXpValues = new HashMap<>();

        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                LocalDate date = task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                dailyXpValues.computeIfAbsent(date, k -> new ArrayList<>()).add(task.calculateXp());
            }
        }

        Map<LocalDate, Float> dailyAverageXp = new HashMap<>();
        for(Map.Entry<LocalDate, List<Integer>> entry : dailyXpValues.entrySet()){
            float sum = 0;
            for(Integer xp : entry.getValue()){
                sum += xp;
            }
            dailyAverageXp.put(entry.getKey(), sum / entry.getValue().size());
        }

        List<Entry> entries = new ArrayList<>();
        List<LocalDate> sortedDates = new ArrayList<>(dailyAverageXp.keySet());
        Collections.sort(sortedDates);

        for (LocalDate date : sortedDates) {
            entries.add(new Entry(date.getDayOfMonth(), dailyAverageXp.get(date)));
        }
        return entries;
    }
}
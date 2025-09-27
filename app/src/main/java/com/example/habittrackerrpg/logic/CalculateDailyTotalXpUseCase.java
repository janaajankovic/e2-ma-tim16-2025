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
import java.util.concurrent.TimeUnit;

public class CalculateDailyTotalXpUseCase {

    public List<Entry> execute(List<Task> tasks) {
        Map<LocalDate, Integer> dailyXp = new HashMap<>();
        long sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);

        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED && task.getDueDate().getTime() >= sevenDaysAgo) {
                LocalDate date = task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                dailyXp.put(date, dailyXp.getOrDefault(date, 0) + task.calculateXp());
            }
        }

        List<Entry> entries = new ArrayList<>();
        List<LocalDate> sortedDates = new ArrayList<>(dailyXp.keySet());
        Collections.sort(sortedDates);

        for (LocalDate date : sortedDates) {
            // Koristimo dan u mesecu kao X osu
            entries.add(new Entry(date.getDayOfMonth(), dailyXp.get(date)));
        }

        return entries;
    }
}
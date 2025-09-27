package com.example.habittrackerrpg.ui.statistics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskImportance;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.data.repository.StatisticsRepository;
import com.example.habittrackerrpg.logic.*;
import com.github.mikephil.charting.data.Entry;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class StatisticsViewModel extends ViewModel {
    private StatisticsRepository statisticsRepository;
    private LiveData<List<Task>> allTasksLiveData;

    private CalculateLongestStreakUseCase calculateLongestStreakUseCase;
    private CalculateActiveDaysUseCase calculateActiveDaysUseCase;
    private CalculateDailyXpUseCase calculateDailyXpUseCase;
    private CalculateOverallAverageDifficultyUseCase calculateOverallAverageDifficultyUseCase;

    private MutableLiveData<Map<TaskStatus, Integer>> tasksByStatus = new MutableLiveData<>();
    private MutableLiveData<Map<String, Integer>> completedTasksByCategory = new MutableLiveData<>();
    private MutableLiveData<Integer> longestStreak = new MutableLiveData<>();
    private MutableLiveData<Integer> activeDays = new MutableLiveData<>();
    private MutableLiveData<List<Entry>> dailyTotalXp = new MutableLiveData<>();
    private MutableLiveData<List<Entry>> dailyAverageXp = new MutableLiveData<>();
    private MutableLiveData<String> averageDifficultyDescription = new MutableLiveData<>();
    private MutableLiveData<String> specialMissions = new MutableLiveData<>();

    public StatisticsViewModel() {
        this.statisticsRepository = new StatisticsRepository();
        this.calculateLongestStreakUseCase = new CalculateLongestStreakUseCase();
        this.calculateActiveDaysUseCase = new CalculateActiveDaysUseCase();
        this.calculateDailyXpUseCase = new CalculateDailyXpUseCase();
        this.calculateOverallAverageDifficultyUseCase = new CalculateOverallAverageDifficultyUseCase();
        this.allTasksLiveData = statisticsRepository.getAllTasks();
        allTasksLiveData.observeForever(this::processTasks);
    }

    private void processTasks(List<Task> tasks) {
        if (tasks == null) return;
        tasksByStatus.setValue(calculateTasksByStatus(tasks));
        completedTasksByCategory.setValue(calculateCompletedTasksByCategory(tasks));
        longestStreak.setValue(calculateLongestStreakUseCase.execute(tasks));
        activeDays.setValue(calculateActiveDaysUseCase.execute(tasks));
        averageDifficultyDescription.setValue(calculateOverallAverageDifficultyUseCase.execute(tasks).description);
        specialMissions.setValue(calculateSpecialMissions(tasks));

        Map<LocalDate, List<Integer>> dailyXpMap = calculateDailyXpUseCase.getDailyXpMap(tasks);
        List<LocalDate> sortedDates = new ArrayList<>(dailyXpMap.keySet());
        Collections.sort(sortedDates);

        List<Entry> totalXpEntries = new ArrayList<>();
        List<Entry> averageXpEntries = new ArrayList<>();
        long sevenDaysAgoMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);

        for (LocalDate date : sortedDates) {
            float dayIndex = date.toEpochDay();
            List<Integer> xpValues = dailyXpMap.get(date);
            int totalXp = 0;
            for (int xp : xpValues) totalXp += xp;
            float averageXp = (float) totalXp / xpValues.size();
            averageXpEntries.add(new Entry(dayIndex, averageXp));

            if (date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() >= sevenDaysAgoMillis) {
                totalXpEntries.add(new Entry(dayIndex, totalXp));
            }
        }
        dailyTotalXp.setValue(totalXpEntries);
        dailyAverageXp.setValue(averageXpEntries);
    }

    public LiveData<Map<TaskStatus, Integer>> getTasksByStatus() { return tasksByStatus; }
    public LiveData<Map<String, Integer>> getCompletedTasksByCategory() { return completedTasksByCategory; }
    public LiveData<Integer> getLongestStreak() { return longestStreak; }
    public LiveData<Integer> getActiveDays() { return activeDays; }
    public LiveData<List<Entry>> getDailyTotalXp() { return dailyTotalXp; }
    public LiveData<List<Entry>> getDailyAverageXp() { return dailyAverageXp; }
    public LiveData<String> getAverageDifficultyDescription() { return averageDifficultyDescription; }
    public LiveData<String> getSpecialMissions() { return specialMissions; }

    private Map<TaskStatus, Integer> calculateTasksByStatus(List<Task> tasks) {
        Map<TaskStatus, Integer> statusCount = new HashMap<>();
        for (Task task : tasks) {
            if (task.getStatus() != null) statusCount.put(task.getStatus(), statusCount.getOrDefault(task.getStatus(), 0) + 1);
        }
        return statusCount;
    }
    private Map<String, Integer> calculateCompletedTasksByCategory(List<Task> tasks) {
        Map<String, Integer> categoryCount = new HashMap<>();
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED && task.getCategoryId() != null) {
                categoryCount.put(task.getCategoryId(), categoryCount.getOrDefault(task.getCategoryId(), 0) + 1);
            }
        }
        return categoryCount;
    }
    private String calculateSpecialMissions(List<Task> tasks) {
        int started = 0, completed = 0;
        for(Task task : tasks){
            if(task.getImportance() == TaskImportance.SPECIAL){
                if(task.getStatus() == TaskStatus.ACTIVE) started++;
                if(task.getStatus() == TaskStatus.COMPLETED) completed++;
            }
        }
        return "Started: " + started + " / Completed: " + completed;
    }
}
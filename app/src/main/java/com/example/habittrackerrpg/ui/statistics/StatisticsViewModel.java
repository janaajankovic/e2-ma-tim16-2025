package com.example.habittrackerrpg.ui.statistics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.MissionStatus;
import com.example.habittrackerrpg.data.model.SpecialMission;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.data.repository.StatisticsRepository;
import com.example.habittrackerrpg.logic.*;
import com.example.habittrackerrpg.ui.tasks.TaskViewModel;
import com.github.mikephil.charting.data.Entry;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class StatisticsViewModel extends ViewModel {
    private final StatisticsRepository statisticsRepository;
    private final LiveData<List<Task>> allTasksLiveData;

    private final ProfileRepository profileRepository;
    private final LiveData<User> userLiveData;
    private final LiveData<List<Category>> categoriesLiveData;
    private final LiveData<List<SpecialMission>> allMissionsLiveData;

    private final CalculateLongestStreakUseCase calculateLongestStreakUseCase;
    private final CalculateActiveDaysUseCase calculateActiveDaysUseCase;
    private final CalculateDailyXpUseCase calculateDailyXpUseCase;
    private final CalculateOverallAverageDifficultyUseCase calculateOverallAverageDifficultyUseCase;

    private final MutableLiveData<Map<TaskStatus, Integer>> tasksByStatus = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> completedTasksByCategory = new MutableLiveData<>();
    private final MutableLiveData<Integer> longestStreak = new MutableLiveData<>();
    private final MutableLiveData<Integer> activeDays = new MutableLiveData<>();
    private final MutableLiveData<List<Entry>> dailyTotalXp = new MutableLiveData<>();
    private final MutableLiveData<List<Entry>> dailyAverageXp = new MutableLiveData<>();
    private final MutableLiveData<String> averageDifficultyDescription = new MutableLiveData<>();
    private final MutableLiveData<String> specialMissions = new MutableLiveData<>();

    public StatisticsViewModel(TaskViewModel taskViewModel) {
        this.statisticsRepository = new StatisticsRepository();
        this.profileRepository = new ProfileRepository();
        this.calculateLongestStreakUseCase = new CalculateLongestStreakUseCase();
        this.calculateActiveDaysUseCase = new CalculateActiveDaysUseCase();
        this.calculateDailyXpUseCase = new CalculateDailyXpUseCase();
        this.calculateOverallAverageDifficultyUseCase = new CalculateOverallAverageDifficultyUseCase();

        this.allTasksLiveData = taskViewModel.getAllTasksAndInstancesForStats();
        this.categoriesLiveData = taskViewModel.getCategories();
        this.userLiveData = profileRepository.getUserLiveData();

        this.allMissionsLiveData = Transformations.switchMap(userLiveData, user -> {
            if (user != null && user.getAllianceId() != null) {
                return statisticsRepository.getAllUserMissions(user.getAllianceId());
            } else {
                MutableLiveData<List<SpecialMission>> emptyData = new MutableLiveData<>();
                emptyData.setValue(new ArrayList<>());
                return emptyData;
            }
        });

        userLiveData.observeForever(user -> processData());
        allTasksLiveData.observeForever(tasks -> processData());
        categoriesLiveData.observeForever(categories -> processData());
        allMissionsLiveData.observeForever(missions -> processData());
    }

    private void processData() {
        List<Task> tasks = allTasksLiveData.getValue();
        User user = userLiveData.getValue();
        List<Category> categories = categoriesLiveData.getValue();
        List<SpecialMission> missions = allMissionsLiveData.getValue();

        if (tasks == null || user == null || categories == null || missions == null) {
            return;
        }

        tasksByStatus.setValue(calculateTasksByStatus(tasks));
        completedTasksByCategory.setValue(calculateCompletedTasksByCategory(tasks, categories));
        longestStreak.setValue(calculateLongestStreakUseCase.execute(tasks));
        activeDays.setValue(calculateActiveDaysUseCase.execute(tasks));
        averageDifficultyDescription.setValue(calculateOverallAverageDifficultyUseCase.execute(tasks, user.getLevel()).description);
        specialMissions.setValue(calculateSpecialMissions(missions));

        Map<LocalDate, List<Integer>> dailyXpMap = calculateDailyXpUseCase.getDailyXpMap(tasks, user.getLevel());
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
            float averageXp = xpValues.isEmpty() ? 0 : (float) totalXp / xpValues.size();
            averageXpEntries.add(new Entry(dayIndex, averageXp));

            if (date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() >= sevenDaysAgoMillis) {
                totalXpEntries.add(new Entry(dayIndex, totalXp));
            }
        }
        dailyTotalXp.setValue(totalXpEntries);
        dailyAverageXp.setValue(averageXpEntries);
    }

    // GETTERI (ostaju isti)
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
            if (task.getStatus() != null) {
                statusCount.put(task.getStatus(), statusCount.getOrDefault(task.getStatus(), 0) + 1);
            }
        }
        return statusCount;
    }

    private Map<String, Integer> calculateCompletedTasksByCategory(List<Task> tasks, List<Category> categories) {
        Map<String, String> categoryIdToNameMap = new HashMap<>();
        for (Category category : categories) {
            categoryIdToNameMap.put(category.getId(), category.getName());
        }

        Map<String, Integer> categoryCount = new HashMap<>();
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED && task.getCategoryId() != null) {
                String categoryName = categoryIdToNameMap.getOrDefault(task.getCategoryId(), "Unknown");
                categoryCount.put(categoryName, categoryCount.getOrDefault(categoryName, 0) + 1);
            }
        }
        return categoryCount;
    }

    private String calculateSpecialMissions(List<SpecialMission> missions) {
        if (missions == null) {
            return "Started: 0 / Completed: 0";
        }

        int started = missions.size();
        int completed = 0;

        for (SpecialMission mission : missions) {
            if (mission.getStatus() == MissionStatus.SUCCESS) {
                completed++;
            }
        }

        return "Started: " + started + " /  Successfully Completed: " + completed;
    }
}
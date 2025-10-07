package com.example.habittrackerrpg.logic;

import android.util.Log;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.data.model.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CalculateUserStatsUseCase {

    private GenerateTaskOccurrencesUseCase generateOccurrencesUseCase = new GenerateTaskOccurrencesUseCase();
    private static final String TAG = "HitChanceCalculator";

    public BattleStats execute(List<Task> allTasks, List<TaskInstance> allInstances, Date oldLevelUpTimestamp, Date _etapaEndDate) {
        Log.d(TAG, "--- Starting Hit Chance Calculation ---");

        Date etapaStartDate = oldLevelUpTimestamp;
        Date etapaEndDate = _etapaEndDate;

        Log.d(TAG, "Calculating for 'Etapa' from: " + etapaStartDate + " to: " + etapaEndDate);

        long totalTasksInStage = 0;

        Log.d(TAG, "--- Counting TOTAL tasks in stage ---");
        for (Task task : allTasks) {
            if (!task.isRecurring() && task.getStatus() != TaskStatus.PAUSED && task.getStatus() != TaskStatus.CANCELLED && task.getDueDate() != null &&
                    isWithinEtapa(task.getDueDate(), etapaStartDate, etapaEndDate)) {
                totalTasksInStage++;
                Log.d(TAG, "Total Counter: Added non-recurring task '" + task.getName() + "' with due date " + task.getDueDate());
            }
        }

        LocalDate startLocalDate = etapaStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = etapaEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<Task> recurringRules = allTasks.stream()
                .filter(t -> t.isRecurring() && t.getStatus() != TaskStatus.PAUSED && t.getStatus() != TaskStatus.CANCELLED)
                .collect(Collectors.toList());

        Log.d(TAG, "Found " + recurringRules.size() + " active recurring task rules to check.");

        for (Task rule : recurringRules) {
            int occurrences = generateOccurrencesUseCase.execute(rule, startLocalDate, endLocalDate).size();
            totalTasksInStage += occurrences;
            if (occurrences > 0) {
                Log.d(TAG, "Total Counter: Added recurring task '" + rule.getName() + "', found " + occurrences + " occurrences in stage.");
            }
        }
        Log.d(TAG, "Final TOTAL tasks in stage: " + totalTasksInStage);

        long completedTasksInStage = 0;
        Log.d(TAG, "--- Counting COMPLETED tasks in stage (with awardedXp > 0) ---");

        for (Task task : allTasks) {
            if (!task.isRecurring() && task.getStatus() == TaskStatus.COMPLETED && task.getCompletedAt() != null &&
                    isWithinEtapa(task.getCompletedAt(), etapaStartDate, etapaEndDate) &&
                    task.getAwardedXp() > 0) {
                completedTasksInStage++;
                Log.d(TAG, "Completed Counter: Added non-recurring task '" + task.getName() + "' with awardedXp: " + task.getAwardedXp());
            }
        }

        for (TaskInstance instance : allInstances) {
            if (instance.getStatus() == TaskStatus.COMPLETED && instance.getCompletedAt() != null &&
                    isWithinEtapa(instance.getCompletedAt(), etapaStartDate, etapaEndDate) &&
                    instance.getAwardedXp() > 0) {
                completedTasksInStage++;
                Log.d(TAG, "Completed Counter: Added instance of task ID '" + instance.getOriginalTaskId() + "' with awardedXp: " + instance.getAwardedXp());
            }
        }
        Log.d(TAG, "Final COMPLETED tasks in stage: " + completedTasksInStage);

        int hitChance = 0;
        if (totalTasksInStage > 0) {
            hitChance = (int) Math.round(((double) completedTasksInStage / totalTasksInStage) * 100);
        }

        Log.d(TAG, "Final Calculation: (" + completedTasksInStage + " / " + totalTasksInStage + ") * 100 = " + hitChance + "%");
        Log.d(TAG, "--- Hit Chance Calculation Finished ---");

        return new BattleStats(hitChance, totalTasksInStage, completedTasksInStage);
    }

    private boolean isWithinEtapa(Date dateToCheck, Date startDate, Date endDate) {
        return !dateToCheck.before(startDate) && !dateToCheck.after(endDate);
    }
}
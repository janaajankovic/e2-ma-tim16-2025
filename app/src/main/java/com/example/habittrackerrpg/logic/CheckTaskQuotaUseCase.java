package com.example.habittrackerrpg.logic;

import android.util.Log;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskDifficulty;
import com.example.habittrackerrpg.data.model.TaskImportance;
import java.util.Calendar;
import java.util.List;
import java.util.Date;

public class CheckTaskQuotaUseCase {

    private static final String TAG = "XpCalculator";
    private XpLevelScaler scaler = new XpLevelScaler();
    public int execute(Task taskToComplete, List<Task> completedTasks, int userLevel) {
        Log.d(TAG, "--------------------");
        Log.d(TAG, "STARTING XP CALCULATION for task: '" + taskToComplete.getName() + "'");
        Log.d(TAG, "-> Base Difficulty: " + taskToComplete.getDifficulty() + ", Base Importance: " + taskToComplete.getImportance());

        int veryEasyCount = 0, normalCount = 0;
        int easyCount = 0, importantCount = 0;
        int hardCount = 0, extremelyImportantCount = 0;
        int extremeCount = 0;
        int specialCount = 0;

        Date now = new Date();
        for (Task completedTask : completedTasks) {
            if (completedTask.getCompletedAt() == null) continue;

            // Dnevne kvote
            if (isSameDay(completedTask.getCompletedAt(), now)) {
                switch (completedTask.getDifficulty()) {
                    case VERY_EASY: veryEasyCount++; break;
                    case EASY: easyCount++; break;
                    case HARD: hardCount++; break;
                }
                switch (completedTask.getImportance()) {
                    case NORMAL: normalCount++; break;
                    case IMPORTANT: importantCount++; break;
                    case EXTREMELY_IMPORTANT: extremelyImportantCount++; break;
                }
            }
            // Nedeljne kvote
            if (isSameWeek(completedTask.getCompletedAt(), now)) {
                if (completedTask.getDifficulty() == TaskDifficulty.EXTREME) {
                    extremeCount++;
                }
            }
            // Mesečne kvote
            if (isSameMonth(completedTask.getCompletedAt(), now)) {
                if (completedTask.getImportance() == TaskImportance.SPECIAL) {
                    specialCount++;
                }
            }
        }

        Log.d(TAG, "QUOTA COUNTS (Today):");
        Log.d(TAG, "-> VeryEasy: " + veryEasyCount + "/5, Normal: " + normalCount + "/5");
        Log.d(TAG, "-> Easy: " + easyCount + "/5, Important: " + importantCount + "/5");
        Log.d(TAG, "-> Hard: " + hardCount + "/2, ExtremelyImportant: " + extremelyImportantCount + "/2");
        Log.d(TAG, "QUOTA COUNTS (Week/Month):");
        Log.d(TAG, "-> Extreme (Week): " + extremeCount + "/1, Special (Month): " + specialCount + "/1");

        int awardedDifficultyXp = 0;
        int awardedImportanceXp = 0;

        int baseDifficultyXp = getBaseDifficultyXp(taskToComplete);
        int baseImportanceXp =  getBaseImportanceXp(taskToComplete);


        // Provera za Težinu (Difficulty)
        switch (taskToComplete.getDifficulty()) {
            case VERY_EASY:
                if (veryEasyCount < 5) awardedDifficultyXp = scaler.calculateScaledXp(baseDifficultyXp, userLevel);
                break;
            case EASY:
                if (easyCount < 5) awardedDifficultyXp = scaler.calculateScaledXp(baseDifficultyXp, userLevel);
                break;
            case HARD:
                if (hardCount < 2) awardedDifficultyXp = scaler.calculateScaledXp(baseDifficultyXp, userLevel);
                break;
            case EXTREME:
                if (extremeCount < 1) awardedDifficultyXp = scaler.calculateScaledXp(baseDifficultyXp, userLevel);
                break;
        }

        // Provera za Bitnost (Importance)
        switch (taskToComplete.getImportance()) {
            case NORMAL:
                if (normalCount < 5) awardedImportanceXp = scaler.calculateScaledXp(baseImportanceXp, userLevel);
                break;
            case IMPORTANT:
                if (importantCount < 5) awardedImportanceXp = scaler.calculateScaledXp(baseImportanceXp, userLevel);
                break;
            case EXTREMELY_IMPORTANT:
                if (extremelyImportantCount < 2) awardedImportanceXp = scaler.calculateScaledXp(baseImportanceXp, userLevel);
                break;
            case SPECIAL:
                if (specialCount < 1) awardedImportanceXp = scaler.calculateScaledXp(baseImportanceXp, userLevel);
                break;
        }


        Log.d(TAG, "AWARDED SCALED XP (User Level " + userLevel + ") -> Difficulty: " + awardedDifficultyXp + ", Importance: " + awardedImportanceXp);

        int totalAwardedXp = awardedDifficultyXp + awardedImportanceXp;
        Log.d(TAG, "TOTAL AWARDED XP: " + totalAwardedXp);

        return totalAwardedXp;
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameWeek(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }

    private boolean isSameMonth(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    private int getBaseDifficultyXp(Task task) {
        if (task.getDifficulty() == null) return 0;
        switch (task.getDifficulty()) {
            case VERY_EASY: return 1;
            case EASY: return 3;
            case HARD: return 7;
            case EXTREME: return 20;
            default: return 0;
        }
    }

    private int getBaseImportanceXp(Task task) {
        if (task.getImportance() == null) return 0;
        switch (task.getImportance()) {
            case NORMAL: return 1;
            case IMPORTANT: return 3;
            case EXTREMELY_IMPORTANT: return 10;
            case SPECIAL: return 100;
            default: return 0;
        }
    }
}
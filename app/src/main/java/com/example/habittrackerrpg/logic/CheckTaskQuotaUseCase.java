package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskDifficulty;
import com.example.habittrackerrpg.data.model.TaskImportance;
import java.util.List;
import java.util.Date;

public class CheckTaskQuotaUseCase {

    public boolean execute(Task taskToComplete, List<Task> completedTasks) {
        TaskDifficulty difficulty = taskToComplete.getDifficulty();
        TaskImportance importance = taskToComplete.getImportance();

        long veryEasyNormalCount = 0;
        long easyImportantCount = 0;
        long hardExtremelyImportantCount = 0;
        long extremeCount = 0;
        long specialCount = 0;

        Date now = new Date();
        for (Task completedTask : completedTasks) {
            if (isSameDay(completedTask.getCompletedAt(), now)) {
                if (completedTask.getDifficulty() == TaskDifficulty.VERY_EASY || completedTask.getImportance() == TaskImportance.NORMAL) {
                    veryEasyNormalCount++;
                }
                if (completedTask.getDifficulty() == TaskDifficulty.EASY || completedTask.getImportance() == TaskImportance.IMPORTANT) {
                    easyImportantCount++;
                }
                if (completedTask.getDifficulty() == TaskDifficulty.HARD || completedTask.getImportance() == TaskImportance.EXTREMELY_IMPORTANT) {
                    hardExtremelyImportantCount++;
                }
            }
            if (isSameWeek(completedTask.getCompletedAt(), now)) {
                if (completedTask.getDifficulty() == TaskDifficulty.EXTREME) {
                    extremeCount++;
                }
            }
            if (isSameMonth(completedTask.getCompletedAt(), now)) {
                if (completedTask.getImportance() == TaskImportance.SPECIAL) {
                    specialCount++;
                }
            }
        }

        if (difficulty == TaskDifficulty.VERY_EASY || importance == TaskImportance.NORMAL) {
            return veryEasyNormalCount < 5;
        }
        if (difficulty == TaskDifficulty.EASY || importance == TaskImportance.IMPORTANT) {
            return easyImportantCount < 5;
        }
        if (difficulty == TaskDifficulty.HARD || importance == TaskImportance.EXTREMELY_IMPORTANT) {
            return hardExtremelyImportantCount < 2;
        }
        if (difficulty == TaskDifficulty.EXTREME) {
            return extremeCount < 1;
        }
        if (importance == TaskImportance.SPECIAL) {
            return specialCount < 1;
        }

        return true;
    }

    private boolean isSameDay(Date date1, Date date2) { /* ... implementacija ... */ return true;}
    private boolean isSameWeek(Date date1, Date date2) { /* ... implementacija ... */ return true;}
    private boolean isSameMonth(Date date1, Date date2) { /* ... implementacija ... */ return true;}
}
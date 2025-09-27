package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskDifficulty;
import com.example.habittrackerrpg.data.model.TaskImportance;

public class CalculateTaskXpUseCase {

    public int execute(Task task, int userLevel) {
        // Osnovne vrednosti za nivo 1
        double baseDifficultyXp = getBaseDifficultyXp(task);
        double baseImportanceXp = getBaseImportanceXp(task);

        // Skaliramo vrednosti na osnovu nivoa korisnika, počevši od nivoa 2
        // Formula: XP prethodnog + XP prethodnog / 2 (množenje sa 1.5)
        for (int i = 1; i < userLevel; i++) {
            baseDifficultyXp += baseDifficultyXp / 2.0;
            baseImportanceXp += baseImportanceXp / 2.0;
        }

        // Zaokruživanje po specifikaciji i sabiranje
        return (int) (Math.round(baseDifficultyXp) + Math.round(baseImportanceXp));
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
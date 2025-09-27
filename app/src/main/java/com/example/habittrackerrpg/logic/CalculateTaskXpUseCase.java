package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;

public class CalculateTaskXpUseCase {

    public int execute(Task task, int userLevel) {
        // Osnovne vrednosti za nivo 1
        int baseDifficultyXp = getBaseDifficultyXp(task);
        int baseImportanceXp = getBaseImportanceXp(task);

        // Skaliramo vrednosti na osnovu nivoa korisnika
        double scaledDifficultyXp = baseDifficultyXp;
        double scaledImportanceXp = baseImportanceXp;

        // Formula: XP prethodnog + XP prethodnog / 2 (množenje sa 1.5)
        for (int i = 1; i < userLevel; i++) {
            scaledDifficultyXp += scaledDifficultyXp / 2.0;
            scaledImportanceXp += scaledImportanceXp / 2.0;
        }

        return (int) (Math.round(scaledDifficultyXp) + Math.round(scaledImportanceXp));
    }

    private int getBaseDifficultyXp(Task task) {
        switch (task.getDifficulty()) {
            case VERY_EASY: return 1;
            case EASY: return 3;
            case HARD: return 7;
            case EXTREME: return 20;
            default: return 0;
        }
    }

    private int getBaseImportanceXp(Task task) {
        switch (task.getImportance()) {
            case NORMAL: return 1;
            case IMPORTANT: return 3;
            case EXTREMELY_IMPORTANT: return 10;
            case SPECIAL: return 100;
            default: return 0;
        }
    }
}
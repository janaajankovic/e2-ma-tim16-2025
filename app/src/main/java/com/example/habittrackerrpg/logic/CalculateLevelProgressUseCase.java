package com.example.habittrackerrpg.logic;

public class CalculateLevelProgressUseCase {

    public static class LevelProgressResult {
        public final int level;
        public final long xpForCurrentLevel; // XP sakupljen u ovom nivou
        public final long xpForNextLevel; // Ukupno XP potrebno za sledeći nivo

        public LevelProgressResult(int level, long xpForCurrentLevel, long xpForNextLevel) {
            this.level = level;
            this.xpForCurrentLevel = xpForCurrentLevel;
            this.xpForNextLevel = xpForNextLevel;
        }
    }

    public LevelProgressResult execute(long totalXp) {
        int level = 1;
        long xpForNextLevel = 200;
        long xpForPreviousLevel = 0;

        while (totalXp >= xpForNextLevel) {
            level++;
            long previousThreshold = xpForNextLevel;
            // Formula iz specifikacije: XP prethodnog * 2 + XP prethodnog / 2
            // Zaokruživanje na sledeću stotinu
            xpForNextLevel = (long) (Math.ceil((xpForNextLevel * 2.5) / 100.0) * 100);
            xpForPreviousLevel = previousThreshold;
        }

        long xpForCurrentLevel = totalXp - xpForPreviousLevel;
        long requiredForThisLevel = xpForNextLevel - xpForPreviousLevel;

        return new LevelProgressResult(level, xpForCurrentLevel, requiredForThisLevel);
    }
}
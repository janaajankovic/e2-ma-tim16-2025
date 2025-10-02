package com.example.habittrackerrpg.logic;

public class BattleStats {
    private final int hitChancePercentage;
    private final long totalTasksInStage;
    private final long completedTasksInStage;

    public BattleStats(int hitChancePercentage, long totalTasksInStage, long completedTasksInStage) {
        this.hitChancePercentage = hitChancePercentage;
        this.totalTasksInStage = totalTasksInStage;
        this.completedTasksInStage = completedTasksInStage;
    }

    public int getHitChancePercentage() {
        return hitChancePercentage;
    }

    public long getTotalTasksInStage() {
        return totalTasksInStage;
    }

    public long getCompletedTasksInStage() {
        return completedTasksInStage;
    }
}
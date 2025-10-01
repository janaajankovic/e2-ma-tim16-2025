package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import java.util.ArrayList;
import java.util.List;

public class CalculateOverallAverageDifficultyUseCase {

    public static class Result {
        public final String description;
        public Result(String description) { this.description = description; }
    }

    public Result execute(List<Task> tasks, int userLevel) {

        List<Task> completedTasks = new ArrayList<>();
        for(Task task : tasks){
            if(task.getStatus() == TaskStatus.COMPLETED){
                completedTasks.add(task);
            }
        }

        if (completedTasks.isEmpty()) return new Result("No completed tasks yet.");

        float totalXp = 0;
        for (Task task : completedTasks) {

            totalXp += task.calculateXp();
        }

        float averageXp = totalXp / completedTasks.size();

        String description;
        if (averageXp <= 4) description = "You mostly solve VERY EASY tasks.";
        else if (averageXp <= 10) description = "You mostly solve EASY tasks.";
        else if (averageXp <= 20) description = "You mostly solve HARD tasks.";
        else description = "You mostly solve EXTREME tasks.";

        return new Result(description);
    }
}
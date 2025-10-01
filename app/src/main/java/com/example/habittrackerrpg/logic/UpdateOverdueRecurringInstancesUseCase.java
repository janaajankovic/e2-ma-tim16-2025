package com.example.habittrackerrpg.logic;

import android.util.Log;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.data.repository.TaskRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpdateOverdueRecurringInstancesUseCase {

    private final GenerateTaskOccurrencesUseCase generateOccurrencesUseCase;

    public UpdateOverdueRecurringInstancesUseCase() {
        this.generateOccurrencesUseCase = new GenerateTaskOccurrencesUseCase();
    }

    public void execute(List<Task> taskRules, List<TaskInstance> allInstances, TaskRepository taskRepository) {
        if (taskRules == null || allInstances == null) return;

        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

        Set<String> existingInstanceKeys = new HashSet<>();
        for (TaskInstance instance : allInstances) {
            LocalDate instanceDate = instance.getInstanceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String key = instance.getOriginalTaskId() + "_" + instanceDate.toString();
            existingInstanceKeys.add(key);
        }

        for (Task rule : taskRules) {
            if (rule.isRecurring()) {

                LocalDate checkStartDate = LocalDate.now().minusDays(90);
                List<LocalDate> pastOccurrences = generateOccurrencesUseCase.execute(rule, checkStartDate, threeDaysAgo);

                for (LocalDate occurrenceDate : pastOccurrences) {
                    String key = rule.getId() + "_" + occurrenceDate.toString();
                    if (!existingInstanceKeys.contains(key)) {
                        Log.d("OverdueCheck", "Found missed recurring task: " + rule.getName() + " on " + occurrenceDate);
                        TaskInstance uncompletedInstance = new TaskInstance(
                                rule.getId(),
                                rule.getUserId(),
                                java.util.Date.from(occurrenceDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                                TaskStatus.UNCOMPLETED
                        );
                        taskRepository.addTaskInstance(uncompletedInstance);
                    }
                }
            }
        }
    }
}
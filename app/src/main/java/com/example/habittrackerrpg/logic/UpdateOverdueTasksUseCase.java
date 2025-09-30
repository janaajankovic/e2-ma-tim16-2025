package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.data.repository.TaskRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UpdateOverdueTasksUseCase {

    public void execute(List<Task> allTasks, TaskRepository taskRepository) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -3);
        Date threeDaysAgo = cal.getTime();

        for (Task task : allTasks) {
            if (task.getStatus() == TaskStatus.ACTIVE && !task.isRecurring() && task.getDueDate() != null) {
                if (task.getDueDate().before(threeDaysAgo)) {
                    task.setStatus(TaskStatus.UNCOMPLETED);
                    taskRepository.updateTask(task);
                }
            }
        }
    }
}
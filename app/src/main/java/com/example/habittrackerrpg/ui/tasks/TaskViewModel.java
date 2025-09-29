package com.example.habittrackerrpg.ui.tasks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.data.repository.CategoryRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.data.repository.TaskRepository;
import com.example.habittrackerrpg.logic.CheckTaskQuotaUseCase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskViewModel extends ViewModel {

    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;
    private ProfileRepository profileRepository;
    private CheckTaskQuotaUseCase checkTaskQuotaUseCase;
    private LiveData<List<Category>> categoriesLiveData;
    private MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private LiveData<List<Task>> tasksLiveData;


    public TaskViewModel() {
        taskRepository = new TaskRepository();
        categoryRepository = new CategoryRepository();
        profileRepository = new ProfileRepository();
        checkTaskQuotaUseCase = new CheckTaskQuotaUseCase();
        categoriesLiveData = categoryRepository.getCategories();
        tasksLiveData = taskRepository.getTasks();
    }

    public LiveData<List<Category>> getCategories() {
        return categoriesLiveData;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void addTask(Task task) {
        // Basic validation
        if (task.getName() == null || task.getName().trim().isEmpty()) {
            toastMessage.setValue("Task name cannot be empty.");
            return;
        }
        if (task.getCategoryId() == null) {
            toastMessage.setValue("Please select a category.");
            return;
        }
        if (task.getDifficulty() == null || task.getImportance() == null) {
            toastMessage.setValue("Please select difficulty and importance.");
            return;
        }
        // TODO: Add quota check logic here using a UseCase

        taskRepository.addTask(task);
        toastMessage.setValue("Task created successfully!");
    }

    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
    }

    public void updateTaskStatus(Task task, TaskStatus newStatus) {
        if (task.getStatus() == TaskStatus.COMPLETED && newStatus == TaskStatus.COMPLETED) {
            toastMessage.setValue("Task is already completed.");
            return;
        }

        task.setStatus(newStatus);

        if (newStatus == TaskStatus.COMPLETED) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            Date todayStart = cal.getTime();

            taskRepository.getCompletedTasksSince(todayStart).observeForever(completedToday -> {
                if (checkTaskQuotaUseCase.execute(task, completedToday)) {
                    profileRepository.addXp(task.getXpValue());
                    toastMessage.setValue("Task completed! +" + task.getXpValue() + " XP");
                } else {
                    toastMessage.setValue("Task completed! XP quota for this type reached for today.");
                }
                taskRepository.updateTask(task);
            });
        } else {
            taskRepository.updateTask(task);
        }
    }
}
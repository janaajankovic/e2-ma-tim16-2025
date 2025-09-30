package com.example.habittrackerrpg.ui.tasks;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.data.repository.CategoryRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.data.repository.TaskRepository;
import com.example.habittrackerrpg.logic.CheckTaskQuotaUseCase;
import com.example.habittrackerrpg.logic.Event;
import com.example.habittrackerrpg.logic.UpdateOverdueTasksUseCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskViewModel extends ViewModel {

    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;
    private ProfileRepository profileRepository;
    private CheckTaskQuotaUseCase checkTaskQuotaUseCase;
    private UpdateOverdueTasksUseCase updateOverdueTasksUseCase;

    private LiveData<List<Category>> categoriesLiveData;
    private MutableLiveData<Event<String>> toastMessage = new MutableLiveData<>();

    private LiveData<List<Task>> sourceTasks;

    private MediatorLiveData<List<Task>> tasksLiveData = new MediatorLiveData<>();

    public TaskViewModel() {
        taskRepository = new TaskRepository();
        categoryRepository = new CategoryRepository();
        profileRepository = new ProfileRepository();
        checkTaskQuotaUseCase = new CheckTaskQuotaUseCase();
        updateOverdueTasksUseCase = new UpdateOverdueTasksUseCase();

        categoriesLiveData = categoryRepository.getCategories();

        sourceTasks = taskRepository.getTasks();

        tasksLiveData.addSource(sourceTasks, tasks -> {
            tasksLiveData.setValue(tasks);
            Log.d("REALTIME_DEBUG", "MediatorLiveData updated from repository. Task count: " + (tasks != null ? tasks.size() : 0));
        });
    }

    private Date getStartOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
    }

    public LiveData<List<Category>> getCategories() {
        return categoriesLiveData;
    }

    public LiveData<Event<String>> getToastMessage() {
        return toastMessage;
    }

    public void addTask(Task task) {
        if (task.getName() == null || task.getName().trim().isEmpty()) {
            toastMessage.setValue(new Event<>("Task name cannot be empty."));
            return;
        }
        if (task.getCategoryId() == null) {
            toastMessage.setValue(new Event<>("Please select a category."));
            return;
        }
        if (task.getDifficulty() == null || task.getImportance() == null) {
            toastMessage.setValue(new Event<>("Please select difficulty and importance."));
            return;
        }

        taskRepository.addTask(task);
        toastMessage.setValue(new Event<>("Task created successfully!"));
    }

    public void updateTaskStatus(Task task, TaskStatus newStatus) {
        if (task.getStatus() == TaskStatus.COMPLETED && newStatus == TaskStatus.COMPLETED) {
            toastMessage.setValue(new Event<>("Task is already completed."));
            return;
        }

        if (task.getStatus() == TaskStatus.UNCOMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            toastMessage.setValue(new Event<>("This task can no longer be changed."));
            return;
        }

        if (newStatus == TaskStatus.PAUSED && !task.isRecurring()) {
            toastMessage.setValue(new Event<>("Only recurring tasks can be paused."));
            return;
        }

        List<Task> currentTasks = tasksLiveData.getValue();
        if (currentTasks == null) {
            currentTasks = new ArrayList<>();
        }

        List<Task> updatedTasks = new ArrayList<>();
        for (Task t : currentTasks) {
            if (t.getId() != null && t.getId().equals(task.getId())) {
                Task updatedTask = new Task(t);
                updatedTask.setStatus(newStatus);
                updatedTasks.add(updatedTask);
            } else {
                updatedTasks.add(t);
            }
        }

        tasksLiveData.setValue(updatedTasks);

        Task taskToUpdate = new Task(task);
        taskToUpdate.setStatus(newStatus);

        if (newStatus == TaskStatus.COMPLETED) {
            Date todayStart = getStartOfToday();

            taskRepository.getCompletedTasksSince(todayStart, completedToday -> {
                if (checkTaskQuotaUseCase.execute(taskToUpdate, completedToday)) {
                    profileRepository.addXp(taskToUpdate.getXpValue());
                    toastMessage.postValue(new Event<>("Task completed! +" + taskToUpdate.getXpValue() + " XP"));
                } else {
                    toastMessage.postValue(new Event<>("Task completed! XP quota for this type reached for today."));
                }
                taskRepository.updateTask(taskToUpdate);
            });
        } else {
            taskRepository.updateTask(taskToUpdate);
        }
    }
}
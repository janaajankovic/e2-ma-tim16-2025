package com.example.habittrackerrpg.ui.tasks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TaskViewModel extends ViewModel {

    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;
    private ProfileRepository profileRepository;
    private CheckTaskQuotaUseCase checkTaskQuotaUseCase;
    private LiveData<List<Category>> categoriesLiveData;
    private MutableLiveData<Event<String>> toastMessage = new MutableLiveData<>();
    private LiveData<List<Task>> tasksLiveData;

    private LiveData<List<Task>> oneTimeTasks;
    private LiveData<List<Task>> recurringTasks;
    private UpdateOverdueTasksUseCase updateOverdueTasksUseCase;

    public TaskViewModel() {
        taskRepository = new TaskRepository();
        categoryRepository = new CategoryRepository();
        profileRepository = new ProfileRepository();
        checkTaskQuotaUseCase = new CheckTaskQuotaUseCase();
        categoriesLiveData = categoryRepository.getCategories();
        updateOverdueTasksUseCase = new UpdateOverdueTasksUseCase();
        tasksLiveData = taskRepository.getTasks();
        oneTimeTasks = Transformations.map(tasksLiveData, tasks -> {
            Date today = getStartOfToday();
            return tasks.stream()
                    .filter(t -> !t.isRecurring() && t.getDueDate() != null && !t.getDueDate().before(today))
                    .collect(Collectors.toList());
        });

        recurringTasks = Transformations.map(tasksLiveData, tasks -> {
            Date today = getStartOfToday();
            return tasks.stream()
                    .filter(t -> t.isRecurring() && t.getRecurrenceEndDate() != null && !t.getRecurrenceEndDate().before(today))
                    .collect(Collectors.toList());
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
    public LiveData<List<Task>> getOneTimeTasks() { return oneTimeTasks; }
    public LiveData<List<Task>> getRecurringTasks() { return recurringTasks; }

    public LiveData<List<Category>> getCategories() {
        return categoriesLiveData;
    }

    public LiveData<Event<String>> getToastMessage() {
        return toastMessage;
    }
    public void addTask(Task task) {
        // Basic validation
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
        // TODO: Add quota check logic here using a UseCase

        taskRepository.addTask(task);
        toastMessage.setValue(new Event<>("Task created successfully!"));
    }

    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
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

        task.setStatus(newStatus);

        task.setStatus(newStatus);

        if (newStatus == TaskStatus.COMPLETED) {
            Date todayStart = getStartOfToday();

            taskRepository.getCompletedTasksSince(todayStart, completedToday -> {
                if (checkTaskQuotaUseCase.execute(task, completedToday)) {
                    profileRepository.addXp(task.getXpValue());
                    toastMessage.postValue(new Event<>("Task completed! +" + task.getXpValue() + " XP"));
                } else {
                    toastMessage.postValue(new Event<>("Task completed! XP quota for this type reached for today."));
                }
                taskRepository.updateTask(task);
            });
        } else {
            taskRepository.updateTask(task);
        }
    }
}
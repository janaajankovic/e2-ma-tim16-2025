package com.example.habittrackerrpg.ui.tasks;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.CategoryRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.data.repository.TaskRepository;
import com.example.habittrackerrpg.logic.CalculateTaskXpUseCase;
import com.example.habittrackerrpg.logic.CheckTaskQuotaUseCase;
import com.example.habittrackerrpg.logic.Event;
import com.example.habittrackerrpg.logic.UpdateOverdueRecurringInstancesUseCase;
import com.example.habittrackerrpg.logic.UpdateOverdueTasksUseCase;
import com.google.firebase.auth.FirebaseAuth;

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
    private UpdateOverdueRecurringInstancesUseCase updateOverdueRecurringInstancesUseCase;
    private FirebaseAuth mAuth;
    private LiveData<List<Category>> categoriesLiveData;
    private MutableLiveData<Event<String>> toastMessage = new MutableLiveData<>();

    private LiveData<List<Task>> taskRulesLiveData;
    private LiveData<List<TaskInstance>> taskInstancesLiveData;
    private MediatorLiveData<List<Task>> allCompletedTasksForStats = new MediatorLiveData<>();
    private final LiveData<User> userLiveData;

    public TaskViewModel() {
        taskRepository = new TaskRepository();
        categoryRepository = new CategoryRepository();
        profileRepository = new ProfileRepository();
        checkTaskQuotaUseCase = new CheckTaskQuotaUseCase();
        updateOverdueTasksUseCase = new UpdateOverdueTasksUseCase();
        updateOverdueRecurringInstancesUseCase = new UpdateOverdueRecurringInstancesUseCase();
        taskRulesLiveData = taskRepository.getTasks();
        taskInstancesLiveData = taskRepository.getTaskInstances();
        categoriesLiveData = categoryRepository.getCategories();
        userLiveData = profileRepository.getUserLiveData();

        allCompletedTasksForStats.addSource(taskRulesLiveData, rules -> combineDataForStats());
        allCompletedTasksForStats.addSource(taskInstancesLiveData, instances -> combineDataForStats());

        MediatorLiveData<Object> overdueChecker = new MediatorLiveData<>();
        overdueChecker.addSource(taskRulesLiveData, value -> overdueChecker.setValue(new Object()));
        overdueChecker.addSource(taskInstancesLiveData, value -> overdueChecker.setValue(new Object()));

        overdueChecker.observeForever(obj -> {
            List<Task> rules = taskRulesLiveData.getValue();
            List<TaskInstance> instances = taskInstancesLiveData.getValue();
            if (rules != null && instances != null) {
                updateOverdueTasksUseCase.execute(rules, taskRepository);
                updateOverdueRecurringInstancesUseCase.execute(rules, instances, taskRepository);
            }
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

    public LiveData<List<Task>> getTaskRules() {
        return taskRulesLiveData;
    }

    public LiveData<List<TaskInstance>> getTaskInstances() {
        return taskInstancesLiveData;
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

    public void updateTaskStatus(Task task, TaskStatus newStatus, Date occurrenceDate) {
        if (task.isRecurring()) {
            if (newStatus == TaskStatus.PAUSED || newStatus == TaskStatus.ACTIVE) {
                Task ruleToUpdate = new Task(task);
                ruleToUpdate.setStatus(newStatus);
                taskRepository.updateTask(ruleToUpdate);
                String message = (newStatus == TaskStatus.PAUSED) ? "Task paused." : "Task activated.";
                toastMessage.postValue(new Event<>(message));
            } else {
                if (newStatus == TaskStatus.COMPLETED) {
                    handleTaskCompletion(task, occurrenceDate);
                } else {
                    TaskInstance instance = new TaskInstance(task.getId(), task.getUserId(), occurrenceDate, newStatus);
                    taskRepository.addTaskInstance(instance);
                }
            }
        } else {
            if (newStatus == TaskStatus.COMPLETED) {
                handleTaskCompletion(task, null); // occurrenceDate nije potreban
            } else {
                Task taskToUpdate = new Task(task);
                taskToUpdate.setStatus(newStatus);
                taskRepository.updateTask(taskToUpdate);
            }
        }
    }

    private void handleTaskCompletion(final Task task, final Date occurrenceDate) {
        User currentUser = userLiveData.getValue();
        if (currentUser == null) {
            Log.e("TaskViewModel", "Cannot handle task completion, user is null.");
            return;
        }

        Observer<List<Task>> observer = new Observer<>() {
            @Override
            public void onChanged(List<Task> updatedCompletedTasks) {
                Log.d("TaskViewModel", "Observer triggered. Completed tasks count: " + updatedCompletedTasks.size());

                int finalXp = checkTaskQuotaUseCase.execute(task, updatedCompletedTasks, currentUser.getLevel());
                profileRepository.addXp(finalXp);
                toastMessage.postValue(new Event<>("Task completed! +" + finalXp + " XP"));

                allCompletedTasksForStats.removeObserver(this);
            }
        };

        allCompletedTasksForStats.observeForever(observer);

        if (task.isRecurring()) {
            TaskInstance instance = new TaskInstance(task.getId(), task.getUserId(), occurrenceDate, TaskStatus.COMPLETED);
            taskRepository.addTaskInstance(instance);
        } else {
            Task taskToUpdate = new Task(task);
            taskToUpdate.setStatus(TaskStatus.COMPLETED);
            taskToUpdate.setCompletedAt(new Date());
            taskRepository.updateTask(taskToUpdate);
        }
    }

    private void combineDataForStats() {
        List<Task> taskRules = taskRulesLiveData.getValue();
        List<TaskInstance> instances = taskInstancesLiveData.getValue();
        if (taskRules == null || instances == null) return;

        List<Task> completedEvents = new ArrayList<>();
        for (Task rule : taskRules) {
            if (!rule.isRecurring() && rule.getStatus() == TaskStatus.COMPLETED) {
                completedEvents.add(rule);
            }
        }
        for (TaskInstance instance : instances) {
            if (instance.getStatus() == TaskStatus.COMPLETED) {
                taskRules.stream()
                        .filter(rule -> rule.getId().equals(instance.getOriginalTaskId()))
                        .findFirst()
                        .ifPresent(rule -> {
                            Task event = new Task(rule);
                            event.setId(instance.getId());
                            event.setStatus(instance.getStatus());
                            event.setCompletedAt(instance.getCompletedAt());
                            event.setDueDate(instance.getInstanceDate());
                            completedEvents.add(event);
                        });
            }
        }
        allCompletedTasksForStats.setValue(completedEvents);
    }

    public LiveData<List<Task>> getAllCompletedTasksForStats() {
        return allCompletedTasksForStats;
    }

    public void editTask(Task originalTask, Task editedTask) {
        if (!originalTask.isRecurring()) {
            editedTask.setId(originalTask.getId());
            editedTask.setUserId(originalTask.getUserId());
            editedTask.setCreatedAt(originalTask.getCreatedAt());
            editedTask.setStatus(originalTask.getStatus());
            editedTask.setXpValue(editedTask.calculateXp());
            taskRepository.updateTask(editedTask);
            toastMessage.postValue(new Event<>("Task updated successfully!"));

        } else {
            Date splitDate = new Date();
            taskRepository.splitRecurringTask(originalTask, editedTask, splitDate);
            toastMessage.postValue(new Event<>("Task updated for all future occurrences!"));
        }
    }

    public void deleteTask(Task task) {
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.UNCOMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            toastMessage.postValue(new Event<>("Cannot delete a completed task."));
            return;
        }

        if (task.isRecurring()) {
            taskRepository.deleteTaskFutureOccurrences(task);
            toastMessage.postValue(new Event<>("Future occurrences have been deleted."));
        } else {
            taskRepository.deleteTask(task.getId());
            toastMessage.postValue(new Event<>("Task deleted successfully."));
        }
    }
}
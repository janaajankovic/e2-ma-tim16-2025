package com.example.habittrackerrpg.ui.tasks;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskDifficulty;
import com.example.habittrackerrpg.data.model.TaskImportance;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.data.repository.AllianceRepository;
import com.example.habittrackerrpg.data.repository.CategoryRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.example.habittrackerrpg.data.repository.TaskRepository;
import com.example.habittrackerrpg.logic.CalculateTaskXpUseCase;
import com.example.habittrackerrpg.logic.CheckTaskQuotaUseCase;
import com.example.habittrackerrpg.logic.Event;
import com.example.habittrackerrpg.logic.UpdateOverdueRecurringInstancesUseCase;
import com.example.habittrackerrpg.logic.UpdateOverdueTasksUseCase;
import com.google.firebase.auth.FirebaseAuth;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {

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
    private MediatorLiveData<List<Task>> allTasksAndInstancesForStats = new MediatorLiveData<>();
    private final AllianceRepository allianceRepository;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        this.allianceRepository = new AllianceRepository(application.getApplicationContext());
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

        allCompletedTasksForStats.addSource(taskRulesLiveData, rules -> combineDataForStats(true));
        allCompletedTasksForStats.addSource(taskInstancesLiveData, instances -> combineDataForStats(true));
        allTasksAndInstancesForStats.addSource(taskRulesLiveData, rules -> combineDataForStats(false));
        allTasksAndInstancesForStats.addSource(taskInstancesLiveData, instances -> combineDataForStats(false));

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

    public LiveData<User> getUserLiveData() {
        return userLiveData;
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
                    TaskInstance instance = new TaskInstance(task.getId(), task.getUserId(), occurrenceDate, newStatus, 0);
                    taskRepository.addTaskInstance(instance);
                }
            }
        } else {
            if (newStatus == TaskStatus.COMPLETED) {
                handleTaskCompletion(task, null);
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
                int calculatedXp = checkTaskQuotaUseCase.execute(task, updatedCompletedTasks, currentUser.getLevel());


                List<Task> allCurrentTasks = taskRulesLiveData.getValue();
                List<TaskInstance> allCurrentInstances = taskInstancesLiveData.getValue();

                profileRepository.addXp(calculatedXp, allCurrentTasks, allCurrentInstances);

                toastMessage.postValue(new Event<>("Task completed! +" + calculatedXp + " XP"));

                if (task.isRecurring()) {
                    TaskInstance instance = new TaskInstance(task.getId(), task.getUserId(), occurrenceDate, TaskStatus.COMPLETED, calculatedXp); // Koristi calculatedXp
                    taskRepository.addTaskInstance(instance);
                } else {
                    Task taskToUpdate = new Task(task);
                    taskToUpdate.setAwardedXp(calculatedXp);
                    taskToUpdate.setStatus(TaskStatus.COMPLETED);
                    taskToUpdate.setCompletedAt(new Date());
                    taskRepository.updateTask(taskToUpdate);
                }

                Log.d("SpecialMissionHook", "Task completed, logging action for special mission.");
                TaskDifficulty difficulty = task.getDifficulty();
                TaskImportance importance = task.getImportance();

                if (difficulty != null && importance != null) {
                    if (difficulty == TaskDifficulty.VERY_EASY || difficulty == TaskDifficulty.EASY) {
                        allianceRepository.logMissionAction("TASK_COMPLETION", 1);
                    }

                    if(importance == TaskImportance.IMPORTANT || importance == TaskImportance.NORMAL) {
                        allianceRepository.logMissionAction("TASK_COMPLETION", 1);
                    }

                    if(difficulty == TaskDifficulty.EASY && importance == TaskImportance.NORMAL){
                        allianceRepository.logMissionAction("TASK_COMPLETION", 1);
                    }
                }

                allCompletedTasksForStats.removeObserver(this);
            }
        };

        allCompletedTasksForStats.observeForever(observer);
    }

    private void combineDataForStats(boolean completedOnly) {
        List<Task> taskRules = taskRulesLiveData.getValue();
        List<TaskInstance> instances = taskInstancesLiveData.getValue();
        if (taskRules == null || instances == null) return;

        List<Task> events = new ArrayList<>();
        for (Task rule : taskRules) {
            if (!rule.isRecurring()) {
                if (!completedOnly || rule.getStatus() == TaskStatus.COMPLETED) {
                    events.add(rule);
                }
            }
        }
        for (TaskInstance instance : instances) {
            if (!completedOnly || instance.getStatus() == TaskStatus.COMPLETED) {
                taskRules.stream()
                        .filter(rule -> rule.getId().equals(instance.getOriginalTaskId()))
                        .findFirst()
                        .ifPresent(rule -> {
                            Task event = new Task(rule);
                            event.setId(instance.getId());
                            event.setStatus(instance.getStatus());
                            event.setCompletedAt(instance.getCompletedAt());
                            event.setDueDate(instance.getInstanceDate());
                            events.add(event);
                        });
            }
        }

        if (completedOnly) {
            allCompletedTasksForStats.setValue(events);
        } else {
            allTasksAndInstancesForStats.setValue(events);
        }
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
            editedTask.setDueDate(editedTask.getDueDate());
            taskRepository.updateTask(editedTask);
            toastMessage.postValue(new Event<>("Task updated successfully!"));

        } else {
            Date splitDate = new Date();
            editedTask.setXpValue(editedTask.calculateXp());
            editedTask.setDueDate(editedTask.getDueDate());
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

     public LiveData<List<Task>> getAllTasksAndInstancesForStats() {
        return allTasksAndInstancesForStats;
    }

}
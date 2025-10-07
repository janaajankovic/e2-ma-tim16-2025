package com.example.habittrackerrpg.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskInstance;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskRepository {
    private static final String TAG = "TaskRepository";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();
    private ListenerRegistration tasksListener;

    private final MutableLiveData<List<TaskInstance>> instancesLiveData = new MutableLiveData<>();
    private ListenerRegistration instancesListener;

    public interface TasksCallback {
        void onCallback(List<Task> tasks);
    }

    public void addTask(Task task) {
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, cannot add task.");
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        task.setUserId(uid);
        task.setCreatedAt(new Date());
        task.setStatus(TaskStatus.ACTIVE);

        task.setXpValue(task.calculateXp());

        db.collection("users").document(uid).collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Task added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding task", e));
    }

    public void addTaskInstance(TaskInstance instance) {
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, cannot add task instance.");
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        instance.setUserId(uid);

        if (instance.getStatus() == TaskStatus.COMPLETED && instance.getCompletedAt() == null) {
            instance.setCompletedAt(new Date());
        }

        db.collection("users").document(uid)
                .collection("tasks").document(instance.getOriginalTaskId())
                .collection("instances")
                .add(instance)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Task instance added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding task instance", e));
    }

    public void getCompletedTasksSince(Date startDate, TasksCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onCallback(new ArrayList<>());
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();

        db.collectionGroup("instances")
                .whereEqualTo("userId", uid)
                .whereEqualTo("status", TaskStatus.COMPLETED)
                .whereGreaterThanOrEqualTo("completedAt", startDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onCallback(new ArrayList<>());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting completed tasks", e);
                    callback.onCallback(new ArrayList<>());
                });
    }

    public LiveData<List<Task>> getTasks() {
        if (mAuth.getCurrentUser() != null && tasksListener == null) {
            String uid = mAuth.getCurrentUser().getUid();
            tasksListener = db.collection("users").document(uid).collection("tasks")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Task rules listen failed.", e);
                            return;
                        }
                        if (snapshots != null) {
                            List<Task> tasks = new ArrayList<>();
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Task task = doc.toObject(Task.class);
                                if (task != null) {
                                    task.setId(doc.getId());
                                    tasks.add(task);
                                }
                            }
                            tasksLiveData.setValue(tasks);
                        }
                    });
        }
        return tasksLiveData;
    }

    public LiveData<List<TaskInstance>> getTaskInstances() {
        if (mAuth.getCurrentUser() != null && instancesListener == null) {
            String uid = mAuth.getCurrentUser().getUid();
            instancesListener = db.collectionGroup("instances")
                    .whereEqualTo("userId", uid)
                    .orderBy("instanceDate", Query.Direction.DESCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Instances listen failed.", e);
                            return;
                        }
                        if (snapshots != null) {
                            instancesLiveData.setValue(snapshots.toObjects(TaskInstance.class));
                        }
                    });
        }
        return instancesLiveData;
    }

    public void updateTask(Task task) {
        if (mAuth.getCurrentUser() == null || task.getId() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        if (task.getStatus() == TaskStatus.COMPLETED && task.getCompletedAt() == null) {
            task.setCompletedAt(new Date());
        }

        db.collection("users").document(uid).collection("tasks").document(task.getId())
                .set(task)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Task successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating task", e));
    }

    public void splitRecurringTask(Task originalTaskRule, Task editedTaskData, Date splitDate) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Calendar cal = Calendar.getInstance();
        cal.setTime(splitDate);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date newEndDateForOldRule = cal.getTime();

        Task newRule = new Task(editedTaskData);
        newRule.setUserId(uid);
        newRule.setCreatedAt(new Date());
        newRule.setRecurring(true);
        newRule.setRecurrenceStartDate(splitDate);
        newRule.setXpValue(newRule.calculateXp());

        WriteBatch batch = db.batch();

        DocumentReference oldRuleRef = db.collection("users").document(uid).collection("tasks").document(originalTaskRule.getId());
        batch.update(oldRuleRef, "recurrenceEndDate", newEndDateForOldRule);

        DocumentReference newRuleRef = db.collection("users").document(uid).collection("tasks").document();
        batch.set(newRuleRef, newRule);

        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Recurring task split successfully."))
                .addOnFailureListener(e -> Log.w(TAG, "Error splitting recurring task", e));
    }

    public void deleteTask(String taskId) {
        if (mAuth.getCurrentUser() == null || taskId == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Task successfully deleted!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting task", e));
    }

    public void deleteTaskFutureOccurrences(Task taskRule) {
        if (mAuth.getCurrentUser() == null || taskRule.getId() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = cal.getTime();

        db.collection("users").document(uid).collection("tasks").document(taskRule.getId())
                .update("recurrenceEndDate", yesterday)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Future occurrences of task deleted."))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating task for deletion", e));
    }
}
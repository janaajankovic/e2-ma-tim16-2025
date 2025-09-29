package com.example.habittrackerrpg.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.habittrackerrpg.logic.CalculateTaskXpUseCase;

public class TaskRepository {
    private static final String TAG = "TaskRepository";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProfileRepository profileRepository = new ProfileRepository();

    public void addTask(Task task) {
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, cannot add task.");
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        task.setUserId(uid);
        task.setCreatedAt(new Date());
        task.setStatus(TaskStatus.ACTIVE);

        profileRepository.getUserLiveData().observeForever(user -> {
            if (user != null) {
                CalculateTaskXpUseCase xpCalculator = new CalculateTaskXpUseCase();
                int currentLevel = user.getLevel();
                int calculatedXp = xpCalculator.execute(task, currentLevel);
                task.setXpValue(calculatedXp);

        db.collection("users").document(uid).collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Task added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding task", e));
            }
        });
    }

    public LiveData<List<Task>> getCompletedTasksSince(Date startDate) {
        MutableLiveData<List<Task>> completedTasksLiveData = new MutableLiveData<>();
        if (mAuth.getCurrentUser() == null) {
            return completedTasksLiveData;
        }
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("tasks")
                .whereEqualTo("status", TaskStatus.COMPLETED)
                .whereGreaterThanOrEqualTo("completedAt", startDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    completedTasksLiveData.setValue(queryDocumentSnapshots.toObjects(Task.class));
                });
        return completedTasksLiveData;
    }

    public LiveData<List<Task>> getTasks() {
        MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();
        if (mAuth.getCurrentUser() == null) {
            return tasksLiveData;
        }
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("tasks")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
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
        return tasksLiveData;
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
}

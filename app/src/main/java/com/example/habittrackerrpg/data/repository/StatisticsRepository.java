package com.example.habittrackerrpg.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.habittrackerrpg.data.model.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class StatisticsRepository {
    private static final String TAG = "StatisticsRepository";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Task>> getAllTasks() {
        MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();

        db.collection("tasks").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Task> tasks = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    tasks.add(document.toObject(Task.class));
                }
                tasksLiveData.setValue(tasks);
                Log.d(TAG, "Uspešno povučeno " + tasks.size() + " zadataka.");
            } else {
                Log.w(TAG, "Greška pri dobavljanju zadataka.", task.getException());
                tasksLiveData.setValue(null);
            }
        });

        return tasksLiveData;
    }
}
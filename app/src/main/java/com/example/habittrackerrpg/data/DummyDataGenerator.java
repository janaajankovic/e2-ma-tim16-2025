package com.example.habittrackerrpg.data;

import android.util.Log;
import com.example.habittrackerrpg.data.model.Category;
import com.example.habittrackerrpg.data.model.Task;
import com.example.habittrackerrpg.data.model.TaskDifficulty;
import com.example.habittrackerrpg.data.model.TaskImportance;
import com.example.habittrackerrpg.data.model.TaskStatus;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DummyDataGenerator {

    private static final String TAG = "DummyDataGenerator";

    public static void generate() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Kreiramo nekoliko lažnih kategorija
        Category health = new Category("Health", "#FF5733");
        Category work = new Category("Work", "#3375FF");
        Category fun = new Category("Fun", "#FFC300");

        // Kreiramo lažne zadatke
        List<Task> tasks = Arrays.asList(
                createTask("Morning Jog", work.getName(), TaskStatus.COMPLETED, TaskDifficulty.EASY, TaskImportance.NORMAL, new Date(System.currentTimeMillis() - 86400000 * 3)), // pre 3 dana
                createTask("Finish report", work.getName(), TaskStatus.COMPLETED, TaskDifficulty.HARD, TaskImportance.IMPORTANT, new Date(System.currentTimeMillis() - 86400000 * 2)), // pre 2 dana
                createTask("Go to the gym", health.getName(), TaskStatus.UNCOMPLETED, TaskDifficulty.EASY, TaskImportance.NORMAL, new Date(System.currentTimeMillis() - 86400000)), // juče
                createTask("Read a book", fun.getName(), TaskStatus.ACTIVE, TaskDifficulty.EASY, TaskImportance.NORMAL, new Date()), // danas
                createTask("Prepare presentation", work.getName(), TaskStatus.ACTIVE, TaskDifficulty.EXTREME, TaskImportance.EXTREMELY_IMPORTANT, new Date()) // danas
        );

        // Upisujemo zadatke u bazu
        for (Task task : tasks) {
            db.collection("tasks").add(task)
                    .addOnSuccessListener(documentReference -> Log.d(TAG, "Task dodat sa ID: " + documentReference.getId()))
                    .addOnFailureListener(e -> Log.w(TAG, "Greška pri dodavanju taska", e));
        }
    }

    private static Task createTask(String name, String categoryId, TaskStatus status, TaskDifficulty difficulty, TaskImportance importance, Date dueDate) {
        Task task = new Task();
        task.setName(name);
        task.setCategoryId(categoryId);
        task.setStatus(status);
        task.setDifficulty(difficulty);
        task.setImportance(importance);
        task.setDueDate(dueDate);
        task.setCreatedAt(new Date());
        task.setRecurring(false);
        return task;
    }
}
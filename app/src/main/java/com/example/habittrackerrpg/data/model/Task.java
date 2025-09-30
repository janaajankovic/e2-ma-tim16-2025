package com.example.habittrackerrpg.data.model;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.io.Serializable;
import java.util.Objects;

public class Task implements Serializable {
    @Exclude
    private String id;
    private String userId;
    private String name;
    private String description;
    private String categoryId;
    private TaskStatus status;
    private TaskDifficulty difficulty;
    private TaskImportance importance;
    private Date createdAt;
    private Date dueDate;
    private Date completedAt;
    private int xpValue;

    // Polja za ponavljajuće zadatke
    private boolean isRecurring;
    private int recurrenceInterval;
    private String recurrenceUnit;
    private Date recurrenceStartDate;
    private Date recurrenceEndDate;

    public Task() {
    }

    public Task(Task other) {
        if (other == null) return;
        this.id = other.id;
        this.userId = other.userId;
        this.name = other.name;
        this.description = other.description;
        this.categoryId = other.categoryId;
        this.status = other.status;
        this.difficulty = other.difficulty;
        this.importance = other.importance;
        this.createdAt = other.createdAt;
        this.dueDate = other.dueDate;
        this.completedAt = other.completedAt;
        this.xpValue = other.xpValue;
        this.isRecurring = other.isRecurring;
        this.recurrenceInterval = other.recurrenceInterval;
        this.recurrenceUnit = other.recurrenceUnit;
        this.recurrenceStartDate = other.recurrenceStartDate;
        this.recurrenceEndDate = other.recurrenceEndDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(TaskDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public TaskImportance getImportance() {
        return importance;
    }

    public void setImportance(TaskImportance importance) {
        this.importance = importance;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public int getRecurrenceInterval() {
        return recurrenceInterval;
    }

    public void setRecurrenceInterval(int recurrenceInterval) {
        this.recurrenceInterval = recurrenceInterval;
    }

    public String getRecurrenceUnit() {
        return recurrenceUnit;
    }

    public void setRecurrenceUnit(String recurrenceUnit) {
        this.recurrenceUnit = recurrenceUnit;
    }

    public Date getRecurrenceStartDate() {
        return recurrenceStartDate;
    }

    public void setRecurrenceStartDate(Date recurrenceStartDate) {
        this.recurrenceStartDate = recurrenceStartDate;
    }

    public Date getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public void setRecurrenceEndDate(Date recurrenceEndDate) {
        this.recurrenceEndDate = recurrenceEndDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public int getXpValue() {
        return xpValue;
    }

    public void setXpValue(int xpValue) {
        this.xpValue = xpValue;
    }

    public int calculateXp() {
        int xpFromDifficulty = 0;
        switch (this.difficulty) {
            case VERY_EASY: xpFromDifficulty = 1; break;
            case EASY: xpFromDifficulty = 3; break;
            case HARD: xpFromDifficulty = 7; break;
            case EXTREME: xpFromDifficulty = 20; break;
        }

        int xpFromImportance = 0;
        switch (this.importance) {
            case NORMAL: xpFromImportance = 1; break;
            case IMPORTANT: xpFromImportance = 3; break;
            case EXTREMELY_IMPORTANT: xpFromImportance = 10; break;
            case SPECIAL: xpFromImportance = 100; break;
        }

        return xpFromDifficulty + xpFromImportance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id) &&
                Objects.equals(name, task.name) &&
                Objects.equals(status, task.status) &&
                Objects.equals(dueDate, task.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, status, dueDate);
    }
}
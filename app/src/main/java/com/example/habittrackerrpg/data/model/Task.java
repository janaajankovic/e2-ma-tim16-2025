package com.example.habittrackerrpg.data.model;

import java.util.Date;

public class Task {
    private String name;
    private String description;
    private String categoryId;
    private TaskStatus status;
    private TaskDifficulty difficulty;
    private TaskImportance importance;
    private Date createdAt;
    private Date dueDate;

    // Polja za ponavljajuće zadatke
    private boolean isRecurring;
    private int recurrenceInterval;
    private String recurrenceUnit;
    private Date recurrenceStartDate;
    private Date recurrenceEndDate;

    public Task() {
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
}
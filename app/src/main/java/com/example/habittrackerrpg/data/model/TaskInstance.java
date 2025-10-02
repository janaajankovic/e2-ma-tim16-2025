package com.example.habittrackerrpg.data.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

public class TaskInstance implements Serializable {

    @Exclude
    private String id;

    private String originalTaskId;
    private String userId;
    private Date instanceDate;
    private TaskStatus status;
    private Date completedAt;
    private int awardedXp;


    public TaskInstance() {
    }

    public TaskInstance(String originalTaskId, String userId, Date instanceDate, TaskStatus status, int awardedXp) {
        this.originalTaskId = originalTaskId;
        this.userId = userId;
        this.instanceDate = instanceDate;
        this.status = status;
        this.awardedXp = awardedXp;
        if (status == TaskStatus.COMPLETED) {
            this.completedAt = new Date();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalTaskId() {
        return originalTaskId;
    }

    public void setOriginalTaskId(String originalTaskId) {
        this.originalTaskId = originalTaskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getInstanceDate() {
        return instanceDate;
    }

    public void setInstanceDate(Date instanceDate) {
        this.instanceDate = instanceDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public int getAwardedXp() {
        return awardedXp;
    }

    public void setAwardedXp(int awardedXp) {
        this.awardedXp = awardedXp;
    }
}

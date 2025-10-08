package com.example.habittrackerrpg.data.model;

import java.util.ArrayList;
import java.util.List;

public class SpecialMissionProgress {
    private String userId;
    private String username;
    private long totalDamageDealt;

    private String avatarId;
    private int shopPurchases;
    private int regularBossHits;
    private int taskCompletions;
    private int otherTaskCompletions;
    private List<String> dailyMessageDates;

    public SpecialMissionProgress() {}

    public SpecialMissionProgress(String userId, String username, String avatarId) {
        this.userId = userId;
        this.username = username;
        this.totalDamageDealt = 0;
        this.shopPurchases = 0;
        this.regularBossHits = 0;
        this.taskCompletions = 0;
        this.otherTaskCompletions = 0;
        this.dailyMessageDates = new ArrayList<>();
        this.avatarId = avatarId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTotalDamageDealt() {
        return totalDamageDealt;
    }

    public void setTotalDamageDealt(long totalDamageDealt) {
        this.totalDamageDealt = totalDamageDealt;
    }

    public int getShopPurchases() {
        return shopPurchases;
    }

    public void setShopPurchases(int shopPurchases) {
        this.shopPurchases = shopPurchases;
    }

    public int getRegularBossHits() {
        return regularBossHits;
    }

    public void setRegularBossHits(int regularBossHits) {
        this.regularBossHits = regularBossHits;
    }

    public int getTaskCompletions() {
        return taskCompletions;
    }

    public void setTaskCompletions(int taskCompletions) {
        this.taskCompletions = taskCompletions;
    }

    public int getOtherTaskCompletions() {
        return otherTaskCompletions;
    }

    public void setOtherTaskCompletions(int otherTaskCompletions) {
        this.otherTaskCompletions = otherTaskCompletions;
    }

    public List<String> getDailyMessageDates() {
        return dailyMessageDates;
    }

    public void setDailyMessageDates(List<String> dailyMessageDates) {
        this.dailyMessageDates = dailyMessageDates;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }
}
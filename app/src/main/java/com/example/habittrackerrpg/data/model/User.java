package com.example.habittrackerrpg.data.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String username;
    private String avatarId;
    private int level;
    private long xp;
    private long pp;
    private long coins;
    private String title;
    private int highestBossDefeatedLevel;

    private Date lastLevelUpTimestamp;
    private int lastStageHitChance;

    private int lastBossFightAttemptLevel;

    public User() {
    }

    public User(String username, String avatarId) {
        this.username = username;
        this.avatarId = avatarId;
        this.level = 1;
        this.xp = 0;
        this.pp = 0;
        this.coins = 0;
        this.title = "Beginner";
        this.highestBossDefeatedLevel = 0;
        this.lastLevelUpTimestamp = new Date();
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarId() { return avatarId; }
    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }

    public long getPp() { return pp; }
    public void setPp(long pp) { this.pp = pp; }

    public long getCoins() { return coins; }
    public void setCoins(long coins) { this.coins = coins; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getHighestBossDefeatedLevel() {
        return highestBossDefeatedLevel;
    }

    public void setHighestBossDefeatedLevel(int highestBossDefeatedLevel) {
        this.highestBossDefeatedLevel = highestBossDefeatedLevel;
    }

    public Date getLastLevelUpTimestamp() {
        return lastLevelUpTimestamp;
    }

    public void setLastLevelUpTimestamp(Date lastLevelUpTimestamp) {
        this.lastLevelUpTimestamp = lastLevelUpTimestamp;
    }

    public int getLastStageHitChance() {
        return lastStageHitChance;
    }

    public void setLastStageHitChance(int lastStageHitChance) {
        this.lastStageHitChance = lastStageHitChance;
    }

    public int getLastBossFightAttemptLevel() {
        return lastBossFightAttemptLevel;
    }

    public void setLastBossFightAttemptLevel(int lastBossFightAttemptLevel) {
        this.lastBossFightAttemptLevel = lastBossFightAttemptLevel;
    }
}
package com.example.habittrackerrpg.data.model;

import java.util.Date;
import com.google.firebase.firestore.Exclude;
import java.util.HashMap;
import java.util.Map;

public class User {
    @Exclude
    private String id;
    private String username;
    private String avatarId;
    private int level;
    private long xp;
    private long pp;
    private long coins;
    private String title;
    private double permanentPpBonusPercent;
    private long totalPp;
    private double totalAttackChanceBonus;
    private int totalExtraAttacks;
    private int highestBossDefeatedLevel;

    private Date lastLevelUpTimestamp;
    private int lastStageHitChance;

    private int lastBossFightAttemptLevel;
    private String allianceId;
    private int successfulMissions;

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
        this.permanentPpBonusPercent = 0.0;
        this.totalPp = 0;
        this.totalAttackChanceBonus = 0.0;
        this.totalExtraAttacks = 0;
        this.successfulMissions = 0;
        this.highestBossDefeatedLevel = 0;
        this.lastLevelUpTimestamp = new Date();
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public double getPermanentPpBonusPercent() { return permanentPpBonusPercent; }
    public void setPermanentPpBonusPercent(double permanentPpBonusPercent) { this.permanentPpBonusPercent = permanentPpBonusPercent; }

    public long getTotalPp() { return totalPp; }
    public void setTotalPp(long totalPp) { this.totalPp = totalPp; }
    public double getTotalAttackChanceBonus() { return totalAttackChanceBonus; }
    public void setTotalAttackChanceBonus(double totalAttackChanceBonus) { this.totalAttackChanceBonus = totalAttackChanceBonus; }
    public int getTotalExtraAttacks() { return totalExtraAttacks; }
    public void setTotalExtraAttacks(int totalExtraAttacks) { this.totalExtraAttacks = totalExtraAttacks; }

    public int getHighestBossDefeatedLevel() {
        return highestBossDefeatedLevel;
    }

    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }


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

    public int getSuccessfulMissions() {
        return successfulMissions;
    }

    public void setSuccessfulMissions(int successfulMissions) {
        this.successfulMissions = successfulMissions;
    }
}
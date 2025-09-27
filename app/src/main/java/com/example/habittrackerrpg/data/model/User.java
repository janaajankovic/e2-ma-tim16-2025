package com.example.habittrackerrpg.data.model;

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
    private Map<String, Long> difficultyXpMap;
    private Map<String, Long> importanceXpMap;

    public User() {
    }

    public User(String username, String avatarId) {
        this.username = username;
        this.avatarId = avatarId;
        // Inicijalne vrednosti prema specifikaciji
        this.level = 1;
        this.xp = 0;
        this.pp = 0; // Korisnik dobija PP tek nakon prvog nivoa
        this.coins = 0;
        this.title = "Beginner"; // Proizvoljna početna titula
        difficultyXpMap = new HashMap<>();
        difficultyXpMap.put("VERY_EASY", 1L);
        difficultyXpMap.put("EASY", 3L);
        difficultyXpMap.put("HARD", 7L);
        difficultyXpMap.put("EXTREME", 20L);

        importanceXpMap = new HashMap<>();
        importanceXpMap.put("NORMAL", 1L);
        importanceXpMap.put("IMPORTANT", 3L);
        importanceXpMap.put("EXTREMELY_IMPORTANT", 10L);
        importanceXpMap.put("SPECIAL", 100L);

    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarId() { return avatarId; }
    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }

    public Map<String, Long> getDifficultyXpMap() { return difficultyXpMap; }
    public void setDifficultyXpMap(Map<String, Long> map) { this.difficultyXpMap = map; }

    public Map<String, Long> getImportanceXpMap() { return importanceXpMap; }
    public void setImportanceXpMap(Map<String, Long> map) { this.importanceXpMap = map; }

    public long getPp() { return pp; }
    public void setPp(long pp) { this.pp = pp; }

    public long getCoins() { return coins; }
    public void setCoins(long coins) { this.coins = coins; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
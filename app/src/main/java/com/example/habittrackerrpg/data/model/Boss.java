package com.example.habittrackerrpg.data.model;

public class Boss {
    private int level;
    private long hp;
    private String name;
    private String lottieAnimationName;

    public Boss() {
    }

    public Boss(int level, long hp, String name, String lottieAnimationName) {
        this.level = level;
        this.hp = hp;
        this.name = name;
        this.lottieAnimationName = lottieAnimationName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getHp() {
        return hp;
    }

    public void setHp(long hp) {
        this.hp = hp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLottieAnimationName() {
        return lottieAnimationName;
    }

    public void setLottieAnimationName(String lottieAnimationName) {
        this.lottieAnimationName = lottieAnimationName;
    }
}

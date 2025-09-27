package com.example.habittrackerrpg.logic;

public class TitleHelper {
    public static String getTitleForLevel(int level) {
        switch (level) {
            case 1: return "Beginner";
            case 2: return "Apprentice";
            case 3: return "Journeyman";
            // TODO: Dodati još titula za više nivoe
            default: return "Veteran";
        }
    }
}
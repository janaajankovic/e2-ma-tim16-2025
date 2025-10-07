package com.example.habittrackerrpg.logic;

public class XpLevelScaler {
    public static int calculateScaledXp(int baseXp, int userLevel) {
        if (userLevel <= 1) {
            return baseXp;
        }

        double scaledXp = baseXp;
        for (int i = 2; i <= userLevel; i++) {
            scaledXp = scaledXp + (scaledXp / 2.0);
        }

        return (int) Math.round(scaledXp);
    }
}

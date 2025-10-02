package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.R;

public class AvatarHelper {
    public static int getAvatarResourceId(String avatarId) {
        if (avatarId == null) return R.drawable.avatar_1;

        switch (avatarId) {
            case "avatar_1": return R.drawable.avatar_1;
            case "avatar_2": return R.drawable.avatar_2;
            case "avatar_3": return R.drawable.avatar_3;
            case "avatar_4": return R.drawable.avatar_4;
            case "avatar_5": return R.drawable.avatar_5;
            default: return R.drawable.avatar_1;
        }
    }
}
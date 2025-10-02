package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.R;

public class AvatarHelper {
    public static int getAvatarResourceId(String avatarId) {
        if (avatarId == null || avatarId.isEmpty()) {
            return R.drawable.avatar_1;
        }
        switch (avatarId) {
            case "avatar1": return R.drawable.avatar_1;
            case "avatar2": return R.drawable.avatar_2;
            case "avatar3": return R.drawable.avatar_3;
            case "avatar4": return R.drawable.avatar_4;
            case "avatar5": return R.drawable.avatar_5;
            default: return R.drawable.avatar_1;
        }
    }
}
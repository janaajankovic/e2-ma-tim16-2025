package com.example.habittrackerrpg.logic;

import com.example.habittrackerrpg.data.model.User;

public class UserSearchResult {
    public final User user;
    public final RelationshipStatus status;

    public UserSearchResult(User user, RelationshipStatus status) {
        this.user = user;
        this.status = status;
    }
}
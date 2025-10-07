package com.example.habittrackerrpg.data.model;

import com.google.firebase.firestore.Exclude;
import java.util.Map;
import java.util.HashMap;

public class Alliance {

    @Exclude
    private String id;
    private String name;
    private String leaderId;
    private String leaderUsername;

    // Mapa članova: Ključ je User ID, Vrednost su podaci o članu
    private Map<String, AllianceMember> members;

    public Alliance() {}

    public Alliance(String name, String leaderId, String leaderUsername, String leaderAvatarId) {
        this.name = name;
        this.leaderId = leaderId;
        this.leaderUsername = leaderUsername;
        this.members = new HashMap<>();
        // Vođa je automatski i prvi član saveza
        this.members.put(leaderId, new AllianceMember(leaderId, leaderUsername, leaderAvatarId));
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLeaderId() { return leaderId; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }
    public String getLeaderUsername() { return leaderUsername; }
    public void setLeaderUsername(String leaderUsername) { this.leaderUsername = leaderUsername; }
    public Map<String, AllianceMember> getMembers() { return members; }
    public void setMembers(Map<String, AllianceMember> members) { this.members = members; }
}
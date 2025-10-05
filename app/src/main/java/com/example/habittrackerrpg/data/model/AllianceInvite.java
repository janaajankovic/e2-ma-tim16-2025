package com.example.habittrackerrpg.data.model;

import com.google.firebase.firestore.Exclude;

public class AllianceInvite {
    @Exclude
    private String id;
    private String allianceId;
    private String allianceName;
    private String inviterUsername;
    private String receiverId;

    public AllianceInvite() {}

    public AllianceInvite(String allianceId, String allianceName, String inviterUsername, String receiverId) {
        this.allianceId = allianceId;
        this.allianceName = allianceName;
        this.inviterUsername = inviterUsername;
        this.receiverId = receiverId;
    }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }
    public String getAllianceName() { return allianceName; }
    public void setAllianceName(String allianceName) { this.allianceName = allianceName; }
    public String getInviterUsername() { return inviterUsername; }
    public void setInviterUsername(String inviterUsername) { this.inviterUsername = inviterUsername; }
}
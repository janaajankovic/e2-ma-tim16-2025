package com.example.habittrackerrpg.data.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import com.google.firebase.firestore.DocumentId;

public class SpecialMission {
    @DocumentId
    private String id;
    private String allianceId;
    private String leaderId;
    @ServerTimestamp
    private Date startDate;
    private Date endDate;
    private long initialBossHp;
    private long currentBossHp;
    private MissionStatus status;

    public SpecialMission() {}

    public SpecialMission(String allianceId, String leaderId, Date endDate, long initialBossHp) {
        this.allianceId = allianceId;
        this.leaderId = leaderId;
        this.endDate = endDate;
        this.initialBossHp = initialBossHp;
        this.currentBossHp = initialBossHp;
        this.status = MissionStatus.ACTIVE;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public long getInitialBossHp() {
        return initialBossHp;
    }

    public void setInitialBossHp(long initialBossHp) {
        this.initialBossHp = initialBossHp;
    }

    public long getCurrentBossHp() {
        return currentBossHp;
    }

    public void setCurrentBossHp(long currentBossHp) {
        this.currentBossHp = currentBossHp;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
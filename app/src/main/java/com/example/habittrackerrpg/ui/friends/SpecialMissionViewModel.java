package com.example.habittrackerrpg.ui.friends;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.habittrackerrpg.data.model.Alliance;
import com.example.habittrackerrpg.data.model.SpecialMission;
import com.example.habittrackerrpg.data.model.SpecialMissionProgress;
import com.example.habittrackerrpg.data.repository.AllianceRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class SpecialMissionViewModel extends AndroidViewModel {
    private AllianceRepository allianceRepository;
    private LiveData<Alliance> allianceLiveData;

    public LiveData<SpecialMission> missionDetails;
    public LiveData<List<SpecialMissionProgress>> allMembersProgress;
    public LiveData<SpecialMissionProgress> myProgress;

    public SpecialMissionViewModel(@NonNull Application application) {
        super(application);
        allianceRepository = new AllianceRepository(application.getApplicationContext());
        allianceLiveData = allianceRepository.getUsersAlliance();

        missionDetails = Transformations.switchMap(allianceLiveData, alliance -> {
            if (alliance != null && alliance.getActiveMissionId() != null) {
                return allianceRepository.getMissionDetails(alliance.getActiveMissionId());
            }
            return new MutableLiveData<>(null);
        });

        allMembersProgress = Transformations.switchMap(allianceLiveData, alliance -> {
            if (alliance != null && alliance.getActiveMissionId() != null) {
                return allianceRepository.getAllMembersProgress(alliance.getActiveMissionId());
            }
            return new MutableLiveData<>(null);
        });

        myProgress = Transformations.switchMap(allianceLiveData, alliance -> {
            if (alliance != null && alliance.getActiveMissionId() != null) {
                return allianceRepository.getMyProgress(alliance.getActiveMissionId());
            }
            return new MutableLiveData<>(null);
        });
    }

    public void startSpecialMission() {
        Alliance currentAlliance = allianceLiveData.getValue();
        if (currentAlliance != null) {
            allianceRepository.startSpecialMission(currentAlliance);
        }
    }
}
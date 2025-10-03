package com.example.habittrackerrpg;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;
import com.example.habittrackerrpg.data.repository.AllianceRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import org.json.JSONObject;

public class HabitTrackerRPGApp extends Application {

    private static final String ONESIGNAL_APP_ID = "549f71e7-ff87-46af-abc5-f49485ee5c64";

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        // Handler koji se pokreće kada se klikne na notifikaciju ili dugme
        OneSignal.getNotifications().addClickListener(result -> {
            Log.d("OneSignal", "Notification clicked!");

            // --- ISPRAVKA JE OVDE ---
            // Ispravan način da se dobije ID kliknutog dugmeta
            String actionId = result.getResult().getActionId();
            JSONObject data = result.getNotification().getAdditionalData();

            if (data != null && "ALLIANCE_INVITE".equals(data.optString("type"))) {
                String inviteId = data.optString("inviteId", null);
                if (inviteId == null) return;

                AllianceRepository repo = new AllianceRepository();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Uzimamo sveže podatke o korisniku pre akcije
                new ProfileRepository().getUserById(uid).observeForever(user -> {
                    if (user != null) {
                        FirebaseFirestore.getInstance().collection("users").document(uid).collection("alliance_invites").document(inviteId).get()
                                .addOnSuccessListener(inviteDoc -> {
                                    if (!inviteDoc.exists()) return; // Provera da li pozivnica još uvek postoji
                                    var invite = inviteDoc.toObject(com.example.habittrackerrpg.data.model.AllianceInvite.class);
                                    if (invite != null) {
                                        invite.setId(inviteDoc.getId());

                                        // Proveravamo koje dugme je kliknuto
                                        if ("accept_button".equals(actionId)) {
                                            Log.d("OneSignal", "Accept button clicked for invite: " + inviteId);

                                            // Provera da li je korisnik već u savezu
                                            if(user.getAllianceId() != null && !user.getAllianceId().isEmpty()){
                                                Toast.makeText(getApplicationContext(), "You are already in an alliance. Please leave it first.", Toast.LENGTH_LONG).show();
                                            } else {
                                                repo.acceptAllianceInvite(invite, user);
                                            }

                                        } else if ("decline_button".equals(actionId)) {
                                            Log.d("OneSignal", "Decline button clicked for invite: " + inviteId);
                                            repo.declineAllianceInvite(invite);
                                        }
                                    }
                                });
                    }
                });
            }
        });

        OneSignal.getNotifications().requestPermission(true, Continue.with(r -> {}));
    }
}
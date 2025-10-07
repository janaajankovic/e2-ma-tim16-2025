package com.example.habittrackerrpg;

import android.app.Application;
import android.content.Intent;
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

        OneSignal.getNotifications().addClickListener(result -> {
            Log.d("OneSignal", "Notification clicked!");

            String actionId = result.getResult().getActionId();
            JSONObject data = result.getNotification().getAdditionalData();
            if (data == null || actionId == null || !("ALLIANCE_INVITE".equals(data.optString("type")))) {
                return;
            }

            String inviteId = data.optString("inviteId", null);
            if (inviteId == null) return;

            if ("decline_button".equals(actionId)) {
                Log.d("OneSignal", "Decline button clicked, handling in background.");
                AllianceRepository repo = new AllianceRepository(getApplicationContext());
                com.example.habittrackerrpg.data.model.AllianceInvite inviteToDecline = new com.example.habittrackerrpg.data.model.AllianceInvite();
                inviteToDecline.setId(inviteId);
                repo.declineAllianceInvite(inviteToDecline);
                return;
            }

            if ("accept_button".equals(actionId)) {
                Log.d("OneSignal", "Accept button clicked, starting MainActivity to handle it.");

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                intent.putExtra("action", "HANDLE_ALLIANCE_INVITE");
                intent.putExtra("inviteId", inviteId);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });

        OneSignal.getNotifications().requestPermission(true, Continue.with(r -> {}));
    }
}
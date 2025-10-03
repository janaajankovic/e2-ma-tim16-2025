package com.example.habittrackerrpg;

import android.app.Application;

import com.onesignal.Continue; // NOVO: Važan import
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class HabitTrackerRPGApp extends Application {

    private static final String ONESIGNAL_APP_ID = "549f71e7-ff87-46af-abc5-f49485ee5c64";

    @Override
    public void onCreate() {
        super.onCreate();

        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        OneSignal.getNotifications().requestPermission(true, Continue.with(r -> {
            if (r.isSuccess()) {
                if (r.getData()) {
                    // Korisnik je prihvatio dozvolu
                } else {
                    // Korisnik je odbio dozvolu
                }
            } else {
                // Došlo je do greške
            }
        }));
    }
}
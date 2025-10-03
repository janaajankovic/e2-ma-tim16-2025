package com.example.habittrackerrpg.logic;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationSender {

    private static final String ONESIGNAL_API_URL = "https://api.onesignal.com/notifications";
    private static final String ONESIGNAL_APP_ID = "549f71e7-ff87-46af-abc5-f49485ee5c64";
    private static final String ONESIGNAL_REST_API_KEY = "os_v2_app_kspxdz77q5dk7k6f6skil3s4mrou6j4dihyesauvn4y4j23r3z63yvqtxz5n6v4s43vlqxbadq5rsvwxvyh464ccxhpvc65vfvk76jq";

    public static void sendNotificationToUser(Context context, String targetUserId, String title, String message) {
        String TAG = "NotificationSenderDebug";

        Log.d(TAG, "--- sendNotificationToUser POKRENUT ---");
        Log.d(TAG, "ID primaoca: " + targetUserId);

        RequestQueue queue = Volley.newRequestQueue(context);

        try {
            JSONObject notificationContent = new JSONObject();
            notificationContent.put("app_id", ONESIGNAL_APP_ID);

            JSONArray targetUserIds = new JSONArray();
            targetUserIds.put(targetUserId);
            notificationContent.put("include_external_user_ids", targetUserIds);

            JSONObject headings = new JSONObject();
            headings.put("en", title);
            notificationContent.put("headings", headings);

            JSONObject contents = new JSONObject();
            contents.put("en", message);
            notificationContent.put("contents", contents);

            Log.d(TAG, "Kreiran JSON objekat za slanje: " + notificationContent.toString());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ONESIGNAL_API_URL, notificationContent,
                    response -> {
                        Log.d(TAG, "--- SUCCESS! OneSignal je prihvatio zahtev. Odgovor: " + response.toString());
                    },
                    error -> {
                        Log.e(TAG, "--- FAILURE! Greška pri slanju zahteva. ---");
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Status kod: " + error.networkResponse.statusCode);
                            try {
                                String body = new String(error.networkResponse.data, "UTF-8");
                                Log.e(TAG, "Odgovor sa servera: " + body);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e(TAG, "Greška (nema odgovora sa servera): " + error.getMessage());
                        }
                    }
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Authorization", "Basic " + ONESIGNAL_REST_API_KEY);
                    return headers;
                }
            };

            queue.add(request);

        } catch (JSONException e) {
            Log.e(TAG, "KRITIČNA GREŠKA pri kreiranju JSON objekta.", e);
        }
    }
}
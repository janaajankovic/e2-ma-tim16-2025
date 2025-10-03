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

    public static void sendNotificationToUser(Context context, String targetUserId, String title, String message, String inviteId, String allianceId) {
        RequestQueue queue = Volley.newRequestQueue(context);
        try {
            JSONObject notificationContent = new JSONObject();
            notificationContent.put("app_id", ONESIGNAL_APP_ID);
            notificationContent.put("include_external_user_ids", new JSONArray().put(targetUserId));
            notificationContent.put("headings", new JSONObject().put("en", title));
            notificationContent.put("contents", new JSONObject().put("en", message));

            JSONObject data = new JSONObject();
            data.put("type", "ALLIANCE_INVITE");
            data.put("inviteId", inviteId);
            data.put("allianceId", allianceId);
            notificationContent.put("data", data);

            // --- NOVO I KLJUČNO: Dodajemo dugmiće ---
            JSONArray buttons = new JSONArray();
            JSONObject acceptButton = new JSONObject();
            acceptButton.put("id", "accept_button");
            acceptButton.put("text", "Accept");
            buttons.put(acceptButton);
            JSONObject declineButton = new JSONObject();
            declineButton.put("id", "decline_button");
            declineButton.put("text", "Decline");
            buttons.put(declineButton);
            notificationContent.put("buttons", buttons);

            // --- NOVO: Notifikacija je "lepljiva" ---
            // 'android_persistent' je pravi ključ za ovo u OneSignal API-ju
            notificationContent.put("android_persistent", true);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ONESIGNAL_API_URL, notificationContent,
                    response -> Log.d("NotificationSender", "Successfully sent notification request."),
                    error -> Log.e("NotificationSender", "Error sending notification: " + error)
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
            e.printStackTrace();
        }
    }
}
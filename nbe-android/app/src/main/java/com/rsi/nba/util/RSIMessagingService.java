package com.rsi.nba.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rsi.nba.MainActivity;
import com.rsi.nba.R;
import com.rsi.nba.plugins.Push;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class RSIMessagingService extends FirebaseMessagingService {

    private static final String TAG = "nba-RSIMessagingService";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, s);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d(TAG, "El message es: " + message.toString());
        super.onMessageReceived(message);
        JSONObject json = new JSONObject();
        try {
            if (message.getNotification() != null) {
                Log.d(TAG, "En el if de getNotification");
                Log.d(TAG, "El title: " + message.getNotification().getTitle());
                Log.d(TAG, "El getBody: " + message.getNotification().getBody());

                json.put("id", message.getFrom());
                json.put("title", message.getNotification().getTitle());
                json.put("msg", message.getNotification().getBody());
            }
            for (Map.Entry<String, String> entry : message.getData().entrySet()) {
                Log.d(TAG, "En el getValue: " +  entry.getValue());
                Log.d(TAG, "En el getKey: " +  entry.getKey());
                json.put(entry.getKey(), entry.getValue());
            }
            if (json != null) {
                Log.d(TAG, "En el ultimo if");
                if(MainActivity.appForeground) {
                    Log.d(TAG, "En el foreground");
                    Push.sendMessage(json);
                } else {
                    Log.d(TAG, "En el background");
                    createNotification(json);
                }
            } else {
                Log.d(TAG, "Error al recibir los mensajes");
            }
        } catch (Exception e) {
            Log.d(TAG, "En el catch");
        }
    }

    private void createNotification(JSONObject json) {
        Log.d(TAG, "En el createNotification");
        Context applicationContext = getApplicationContext();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = null;

        Intent intent = new Intent(applicationContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelID;
            List<NotificationChannel> channels = mNotificationManager.getNotificationChannels();

            if (channels.size() == 1) {
                channelID = channels.get(0).getId();
                Log.d(TAG, "Hay channel ID: " + channelID);
            } else {
                Log.d(TAG, "No hay channel ID");
                String id = "com.rsi.nba";
                NotificationChannel channel = new NotificationChannel(id,id, NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(channel);
                channelID = id;
            }
            mBuilder = new NotificationCompat.Builder(applicationContext, channelID);

        } else {
            mBuilder = new NotificationCompat.Builder(applicationContext);
        }

        try {
            mBuilder
                    .setSmallIcon(R.mipmap.ic_launcher)
                    //.setContentTitle("NBA")
                    .setContentText(json.getString("msg"))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(json.getString("msg")))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

        } catch (JSONException e) {
            Log.d(TAG, "En el JSONException: " + e);
            e.printStackTrace();
        }

        // Push notification in notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, mBuilder.build());
    }
}
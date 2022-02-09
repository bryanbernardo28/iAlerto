package com.example.ialerto;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class NotificationMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("checkNotif","onMessageReceived");
        Map<String,String> notif = remoteMessage.getData();
        Log.d("checkNotif","Message" + notif.get("body"));
        notifyThis(notif);

    }




    private void notifyThis(Map<String,String> data){

        String CHANNEL_ID = "global_channel";
        String message = data.get("body");
        String title = data.get("title");
        String from_activity = data.get("from_activity");
        String role_message = data.get("role");

        Intent i = new Intent(this, Dashboard.class);
        i.putExtra("from_activity",from_activity);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "notify_001");

        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setAutoCancel(true);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setVibrate(new long[]{800, 800, 800, 800});
        mBuilder.setLights(Color.WHITE,10000,10000);
        mBuilder.setGroup(CHANNEL_ID);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        mBuilder.setAutoCancel(true);
        mBuilder.setGroupSummary(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "iAlertoChannel",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(CHANNEL_ID);
        }
//        mNotificationManager.notify(0, mBuilder.build());

        SharedPreferences loginlogout_pref = getSharedPreferences(MainActivity.LOGINLOGOUTPREF_NAME, Context.MODE_PRIVATE);
//        SharedPreferences myprofile = getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
//        String role = myprofile.getString("role","");
        boolean loggedin = loginlogout_pref.getBoolean("loggedin",false);
        if (loggedin){
            mNotificationManager.notify(0, mBuilder.build());
        }
    }
}

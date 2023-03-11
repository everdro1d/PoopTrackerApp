package com.everdro1d.pooptracker;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlarmReceiverNotification extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        showNotification(context);
    }

    private void showNotification(Context context) {
        NotificationManager mNotificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_pooper_notif)
                .setContentTitle(context.getResources().getString(R.string.notificationTitle))
                .setContentText(context.getResources().getString(R.string.notificationContent))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);
    }
}
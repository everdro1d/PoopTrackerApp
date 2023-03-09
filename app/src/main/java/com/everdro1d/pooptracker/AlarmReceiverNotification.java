package com.everdro1d.pooptracker;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class AlarmReceiverNotification extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        showNotification(context, intent);
    }

    private void showNotification(Context context, Intent intent) {
        NotificationManager mNotificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        NotificationCompat.Builder mBuilder
                = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_pooper_notif)
                .setContentTitle(context.getResources().getString(R.string.notifTitle))
                .setContentText(context.getResources().getString(R.string.notifContent))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);
    }
}
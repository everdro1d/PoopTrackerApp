package com.everdro1d.pooptracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

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


        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_IMMUTABLE);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify(1, mBuilder.build());

    }
}
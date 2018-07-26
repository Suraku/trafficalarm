package com.suraku.trafficalarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.suraku.trafficalarm.activities.MainActivity;

/**
 * Management of notifications
 */

public final class Notifications
{
    public static final int NOTIFICATION_ID_ALARM_SET = 1;
    public static final int NOTIFICATION_ID_ALARM_TRIGGER = 2;


    public static void cancelAlarmTrigger(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID_ALARM_TRIGGER);
    }

    public static void cancelAlarmSet(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID_ALARM_SET);
    }

    public static void createAlarmTrigger(Context context, Intent intent)
    {
        // Prepare notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setStyle(new NotificationCompat.InboxStyle())
                .setContentText(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_alarm_clock);

        // Target to display onClick
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        // Display notification
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(Notifications.NOTIFICATION_ID_ALARM_TRIGGER, notification);
    }

    public static void createAlarmSet(Context context, String contentText)
    {
        Logging.logDebugEvent(context, "Notifications_createAlarmSet");

        // Prepare notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setStyle(new NotificationCompat.InboxStyle())
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_alarm_clock);

        // Target to display onClick
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        // Display notification
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(Notifications.NOTIFICATION_ID_ALARM_SET, notification);
    }
}

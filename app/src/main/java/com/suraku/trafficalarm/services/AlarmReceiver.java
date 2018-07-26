package com.suraku.trafficalarm.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.Notifications;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.viewmodels.MainViewModel;

import java.util.Calendar;
import java.util.Random;

/**
 * Triggered upon alarm, create the Intent service to query the network
 */

public class AlarmReceiver extends WakefulBroadcastReceiver
{
    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmIntent;
    private final int INTENT_ID = 4526286;

    @Override
    public void onReceive(Context context, Intent intent) {
        Logging.logDebugEvent(context, "AlarmReceiver_onReceive");
        Intent service = new Intent(context, AlarmIntentService.class);

        // Tells the alarm whether to skip API request and use the latest request instead
        if (intent.getExtras() != null) {
            boolean skipApiRequest = intent.getExtras().getBoolean(context.getString(R.string.key_serviceAlarm_skipApiRequest), false);

            Bundle args = new Bundle();
            args.putBoolean(context.getString(R.string.key_serviceAlarm_skipApiRequest), skipApiRequest);
            service.putExtras(args);
        }

        // Begin...
        startWakefulService(context, service);
    }

    public boolean isAlarmSet(Context context) {
        return _checkIsAlarmSet(context);
    }

    /*
     * Find the frequency block factor, ensuring the block multiples fit within the time gap
     * Always aims to start with the highest factor value, and decreases if not within current time.
     */
    public int getApiBlockFactor(Calendar alarmTime, Calendar timeNow)
    {
        int factor = AlarmHelper.maxNumberApiChecks;

        for (int i = factor; i >= 0; i--) {
            Calendar cal = (Calendar) alarmTime.clone();
            cal.add(Calendar.SECOND, -1 * i * (int)(long)AlarmHelper.triggerFrequencySec);

            // Must always be in the future
            if (cal.getTimeInMillis() > timeNow.getTimeInMillis()) {
                factor = i;
                break;
            }
        }
        return factor;
    }

    public void setAlarmSpecificTime(Context context, MainViewModel model,
                                     long timeToTriggerMilli, boolean skipApiRequest) {
        Logging.logDebugEvent(context, "AlarmReceiver_setAlarmSpecificTime - ApiSkipRequest set to " + skipApiRequest + ".");
        _setAlarm(context, timeToTriggerMilli, model.getAlarmTime(),
                _getIntent(context, skipApiRequest), false);
    }

    public void setAlarmSpecificTimeExact(Context context, MainViewModel model,
                                     long timeToTriggerMilli, boolean skipApiRequest) {
        Logging.logDebugEvent(context, "AlarmReceiver_setAlarmSpecificTimeExact - ApiSkipRequest set to " + skipApiRequest + ".");
        _setAlarm(context, timeToTriggerMilli, model.getAlarmTime(),
                _getIntent(context, skipApiRequest), true);
    }

    public void setAlarm(Context context, MainViewModel model) {
        // Setup alarm
        Long triggerFreqSec = AlarmHelper.triggerFrequencySec;

        Calendar timeNow = Calendar.getInstance();
        Calendar alarmTime = AlarmHelper.getAlarmTime(model.getAlarm(), (Calendar)timeNow.clone());

        int factor = getApiBlockFactor(alarmTime, timeNow);

        // Determine trigger time
        alarmTime.add(Calendar.SECOND, -1 * factor * (int)(long)triggerFreqSec);
        long numSecsToAlarmTrigger = (alarmTime.getTimeInMillis() - timeNow.getTimeInMillis()) / 1000;

        // Set the alarm
        _setAlarm(context, timeNow.getTimeInMillis() + (numSecsToAlarmTrigger * 1000),
                model.getAlarmTime(), new Intent(context, AlarmReceiver.class), false);
    }

    /*
     Disable any pending alarms
     */
    public void cancelAlarm(Context context) {
        Logging.logEvent(context, context.getString(R.string.logging_alarmCanceled), EventLevel.LOW);

        // If alarm exists, cancel it
        _cancelPendingIntent(context);

        // Disable {@code AppBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, AppBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        // Remove notification
        Notifications.cancelAlarmSet(context);
    }


    /** Private Methods **/

    private void _cancelPendingIntent(Context context)
    {
        if (mAlarmManager != null) {
            mAlarmManager.cancel(mAlarmIntent);
        }

        // Ensure intent also canceled
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, INTENT_ID, intent,
                PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null) {
            pendingIntent.cancel();
        }
    }

    /*
     Prepare and set the alarm. Optional intent to allow for custom bundles.
     */
    private void _setAlarm(Context context, long triggerAtMillis,
                           String notificationText, Intent intent, boolean setExact)
    {
        // Initial setup
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        _cancelPendingIntent(context);
        mAlarmIntent = PendingIntent.getBroadcast(context, INTENT_ID, intent, 0);

        // Generate randomness with more restrictions than API method
        if (!setExact) {
            Random rnd = new Random();
            triggerAtMillis += rnd.nextInt(AlarmHelper.setAlarmTimeVariationSecs) * 1000;  // Keep positive
        }

        // Event log
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(triggerAtMillis);

        String eventMsg = context.getString(R.string.logging_alarmSetForTime) + " "
                + Helper.getDateTimeFormatted(cal, false);
        Logging.logEvent(context, eventMsg, EventLevel.LOW);
        Log.d("APP", String.valueOf(cal.get(Calendar.SECOND)) + " seconds.");


        // Set the alarm
        if (Build.VERSION.SDK_INT >= 23) {
            // Wakes in Doze Mode
            mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, mAlarmIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            // Wakes in Idle Mode
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, mAlarmIntent);
        } else {
            // Old APIs
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, mAlarmIntent);
        }


        // Allow on-reboot of the alarm if enabled
        ComponentName receiver = new ComponentName(context, AppBootReceiver.class);
        PackageManager packageManager = context.getPackageManager();

        packageManager.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        // Show permanent notification
        Notifications.createAlarmSet(context, notificationText);
    }

    private boolean _checkIsAlarmSet(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, INTENT_ID, intent,
                PendingIntent.FLAG_NO_CREATE);

        return (pendingIntent != null);
    }

    private Intent _getIntent(Context context, boolean skipApiRequest)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        Bundle args = new Bundle();
        args.putBoolean(context.getString(R.string.key_serviceAlarm_skipApiRequest), skipApiRequest);
        intent.putExtras(args);

        return intent;
    }

    private String _getTime(long timeToTriggerMilli)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeToTriggerMilli);

        return cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
    }
}

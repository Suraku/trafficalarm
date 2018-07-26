package com.suraku.trafficalarm.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.viewmodels.MainViewModel;

/**
 * This BroadcastReceiver automatically (re)starts the alarm when the device is
 * rebooted. This receiver is set to be disabled (android:enabled="false") in the
 * application's manifest file. When the user sets the alarm, the receiver is enabled.
 * When the user cancels the alarm, the receiver is disabled, so that rebooting the
 * device will not trigger this receiver.
 */
public class AppBootReceiver extends BroadcastReceiver
{
    AlarmReceiver alarm = new AlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Logging.logEvent(context, context.getString(R.string.logging_appBootReceiver), EventLevel.LOW);

            MainViewModel model = new MainViewModel(context, Helper.getActiveAlarm(context));
            alarm.setAlarm(context, model);
        }
    }
}

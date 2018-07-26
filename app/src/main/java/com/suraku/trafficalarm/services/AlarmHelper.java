package com.suraku.trafficalarm.services;

import com.suraku.trafficalarm.models.Alarm;

import java.util.Calendar;

public class AlarmHelper
{
    static final int maxNumberApiChecks = 2;
    static final long triggerFrequencySec = 600;
    static final int setAlarmTimeVariationSecs = 45;

    static Calendar getAlarmTime(Alarm model, Calendar alarmTime)
    {
        if (alarmTime == null) {
            alarmTime = Calendar.getInstance();
        }

        alarmTime.set(Calendar.HOUR_OF_DAY, model.getHour());
        alarmTime.set(Calendar.MINUTE, model.getMinute());
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        Calendar timeNow = Calendar.getInstance();
        timeNow.set(Calendar.SECOND, 0);
        timeNow.set(Calendar.MILLISECOND, 0);

        // Check if the alarm time is set to the past
        if (timeNow.getTimeInMillis() > alarmTime.getTimeInMillis()) {
            alarmTime.add(Calendar.HOUR, 24);   // set to tomorrow
        }

        return alarmTime;
    }
}

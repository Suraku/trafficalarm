package com.suraku.trafficalarm.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.suraku.trafficalarm.ApiHttp;
import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.Notifications;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.activities.AlarmActivity;
import com.suraku.trafficalarm.activities.MainActivity;
import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.data.storage.DataStorageFactory;
import com.suraku.trafficalarm.data.storage.ILocalStorageProvider;
import com.suraku.trafficalarm.models.Address;
import com.suraku.trafficalarm.models.TimeRequest;
import com.suraku.trafficalarm.viewmodels.ApiGMapResult;
import com.suraku.trafficalarm.viewmodels.MainViewModel;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */

public class AlarmIntentService extends IntentService
{
    public AlarmIntentService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Logging.logDebugEvent(this, "AlarmIntentService_onHandleIntent");

        Context context = getApplicationContext();
        MainViewModel model = new MainViewModel(context, Helper.getActiveAlarm(context));

        ILocalStorageProvider<Address> addressRepository = DataStorageFactory.getProvider(this, Address.class);
        ILocalStorageProvider<TimeRequest> timeRepository = DataStorageFactory.getProvider(this, TimeRequest.class);

        Boolean skipApi = false;
        if (intent.getExtras() != null) {
            skipApi = intent.getExtras().getBoolean(getString(R.string.key_serviceAlarm_skipApiRequest), false);
        }
        String json;

        // Begin API request
        if (skipApi && timeRepository.findAll().size() > 0) {
            List<TimeRequest> requests = timeRepository.findAll();
            Collections.sort(requests);
            json = requests.get(0).getJsonResponse();
        } else {
            String url = ApiHttp.createUrl(context, addressRepository.find(model.getAlarm().getOriginAddressFK()),
                    addressRepository.find(model.getAlarm().getDestinationAddressFK()));
            ApiGMapResult result = ApiHttp.getGMapsAPiRequest(context, url);
            json = result.getJSONString();
        }

        // Bring application to front, update time request list
        Bundle args = new Bundle();
        if (!skipApi) {
            args.putString(getString(R.string.key_apiJsonResult), json);
        }

        // Process results
        _processResults(context, model, json, skipApi);

        // Finish
        AlarmReceiver.completeWakefulIntent(intent);
    }

    private void _processResults(Context context, MainViewModel model, String json, boolean skipApi)
    {
        Logging.logDebugEvent(this, "AlarmIntentService_processResults");

        // Gather data (seconds)
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Long triggerFreqSec = AlarmHelper.triggerFrequencySec;
        Long triggerThresholdSec = (preferences.getInt(
                context.getString(R.string.pref_key_alarmDurationThreshold),
                getResources().getInteger(R.integer.pref_default_alarmDurationThreshold)
        ) + 1) * (long)60;
        Boolean isTrafficOnly = preferences.getBoolean(
                context.getString(R.string.pref_key_isOnlyTrafficAlerts),
                Boolean.parseBoolean(context.getString(R.string.pref_default_isOnlyTrafficAlerts))
        );
        Long durationTrafficSec = Long.valueOf(Helper.findJsonObject(Integer.class, json, ApiHttp.KEY_DURATION_TRAFFIC));
        Long durationNormalSec = Long.valueOf(Helper.findJsonObject(Integer.class, json, ApiHttp.KEY_DURATION_NORMAL));
        Long durationDiffSec = durationTrafficSec - durationNormalSec;

        Calendar timeNow = Calendar.getInstance();
        Calendar alarmTime = AlarmHelper.getAlarmTime(model.getAlarm(), (Calendar)timeNow.clone());


        /* Process results */
        AlarmReceiver alarm = new AlarmReceiver();
        long timeLeftMilli = alarmTime.getTimeInMillis() - timeNow.getTimeInMillis();

        boolean breaksThreshold = (durationDiffSec >= triggerThresholdSec);
        boolean withinFinalBlock = (timeLeftMilli <= triggerFreqSec * 1000);

        // Overwrite as system time may be past alarm time, as skipping implies no more requests necessary.
        if (skipApi) withinFinalBlock = true;

        // Determine end result...
        if (withinFinalBlock) {
            if (breaksThreshold) {
                // Sound alarm and display to user.
                _triggerAlarm(json);
            }
            else if (!isTrafficOnly && timeLeftMilli > 0) {
                // Threshold not broken, but we do want to sound the alarm regardless, prepare next
                // trigger as we're not yet at the intended alarm time.
                alarm.setAlarmSpecificTimeExact(context, model, alarmTime.getTimeInMillis(), true);
                return;
            }
            else if (!isTrafficOnly && timeLeftMilli <= 0) {
                // Not broken threshold, however user still desires alarm to trigger.
                _triggerAlarm(json);
            }

            // Prepare tomorrows alarm
            _setAlarm24Hour(context, model, alarm, triggerFreqSec.intValue());

        } else {
            // Check if duration has overlapped the final trigger check
            // i.e. if current check is 3rd, will it overlap final + partially 2nd?
            // If alarm should trigger within 2nd-to-last block, trigger it now.
            // Example: Alarm => 8:30, Triggers at 8:10 and 8:20, traffic duration => 15 min
            // At 8:10 trigger, we see alarm MUST trigger at or before 8:15, so do it now.

            int factor = alarm.getApiBlockFactor(alarmTime, timeNow);
            Calendar alarmClone = (Calendar)alarmTime.clone();
            alarmClone.add(Calendar.SECOND, -1 * factor * triggerFreqSec.intValue());
            long nextAlarmBlockTotalDurationMilli = alarmTime.getTimeInMillis() - alarmClone.getTimeInMillis();

            if (durationDiffSec * 1000 > nextAlarmBlockTotalDurationMilli) {
                // Sound alarm and display to user
                _triggerAlarm(json);

                // Prepare tomorrows alarm
                _setAlarm24Hour(context, model, alarm, triggerFreqSec.intValue());

            } else {
                // Prepare for next API time block check
                Logging.logDebugEvent(context, "AlarmIntentService - Prepare to set alarm for next time block.");
                alarm.setAlarm(context, model);
            }
        }
    }

    private void _triggerAlarm(String json)
    {
        // Logging
        Logging.logEvent(this, getString(R.string.logging_alarmTrigger), EventLevel.LOW);

        /* Display result to user */
        Bundle args = new Bundle();
        args.putString(getString(R.string.key_apiJsonResult), json);

        // Get the main view in the background
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainIntent.putExtras(args);
        startActivity(mainIntent);

        // Sound alarm
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtras(args);
        startActivity(alarmIntent);

        // Create notification
        Notifications.createAlarmTrigger(getApplicationContext(), alarmIntent);
    }

    private void _setAlarm24Hour(Context context, MainViewModel model, AlarmReceiver alarm, int triggerFreqSec) {
        Calendar alarmTime = AlarmHelper.getAlarmTime(model.getAlarm(), null);
        alarmTime.add(Calendar.HOUR, 24);
        alarmTime.add(Calendar.SECOND, -1 * triggerFreqSec * AlarmHelper.maxNumberApiChecks);
        alarm.setAlarmSpecificTime(context, model, alarmTime.getTimeInMillis(), false);
    }
}

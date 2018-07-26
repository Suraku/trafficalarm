package com.suraku.trafficalarm.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.activities.BaseActivity;
import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.preferences.AlarmAudioPreference;

public class Settings_PreferenceFragment extends PreferenceFragment
{
    /* Default constructor */
    public Settings_PreferenceFragment() { }

    public static Settings_PreferenceFragment newInstance() {
        return new Settings_PreferenceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences from XML resource
        addPreferencesFromResource(R.xml.preferences);

        CheckBoxPreference customAlarmPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_customAlarmEnabled));
        customAlarmPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Permission check
                int isPermEnabled = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (isPermEnabled != PackageManager.PERMISSION_GRANTED && (boolean)newValue) {
                    // Prompt user input to accept
                    Logging.logEvent(getContext(), getString(R.string.logging_preferenceFrag_customAlarmPermissionDenied), EventLevel.MED);
                    ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                            BaseActivity.READ_EXTERNAL_STORAGE_REQUEST_CODE);
                    return false;
                }

                // Event log
                _checkboxEventLog((boolean)newValue);
                return true;
            }
        });

        CheckBoxPreference trafficAlertsOnlyPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_isOnlyTrafficAlerts));
        trafficAlertsOnlyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                _checkboxEventLog((boolean)newValue);
                return true;
            }
        });

        ListPreference trafficModelPref = (ListPreference) findPreference(getString(R.string.pref_key_trafficModel));
        trafficModelPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String eventMsg = getString(R.string.logging_preferenceFrag_trafficModelChanged)
                        + " " + newValue + ".";
                Logging.logEvent(getContext(), eventMsg, EventLevel.LOW);
                return true;
            }
        });
    }

    @Override
    public void onStop() {
        _stopMedia();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        _stopMedia();
        super.onDestroy();
    }

    private void _stopMedia() {
        AlarmAudioPreference preference = (AlarmAudioPreference) findPreference(getString(R.string.pref_key_alarmAudioName));
        preference.stopMedia();
        Logging.logDebugEvent(getContext(), "Settings_PreferenceFragment_stopMedia");
    }

    private void _checkboxEventLog(boolean newValue) {
        String eventMsg = getString(R.string.logging_preferenceFrag_customAlarmChangedEndText);
        if (newValue) {
            eventMsg = getString(R.string.value_enabled) + " " + eventMsg;
        } else {
            eventMsg = getString(R.string.value_disabled) + " " + eventMsg;
        }
        Logging.logEvent(getContext(), eventMsg, EventLevel.LOW);
    }
}

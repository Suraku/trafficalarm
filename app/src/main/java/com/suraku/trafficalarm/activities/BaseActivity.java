package com.suraku.trafficalarm.activities;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.fragments.Settings_PreferenceFragment;

/**
 * Base activity class functionality for all activities
 */

public class BaseActivity extends FragmentActivity
{
    public static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        // Global error logging
        final Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                // Log error
                try {
                    Logging.logErrorEvent(mContext, (Exception)throwable);
                } catch (Exception e) {
                    // Ignore and let the framework handle it.
                }

                // Default functionality
                handler.uncaughtException(thread, throwable);
            }
        });

        // Check permissions
        int isPermEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (isPermEnabled != PackageManager.PERMISSION_GRANTED) {
            _updatePrefIsCustomAlarm(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Application-wide permission checking
        Logging.logDebugEvent(this, "BaseActivity_onRequestPermissionsResult");

        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST_CODE:
                // Permission will only purposely be granted when wanting to enable checkbox
                if (grantResults[0] == 0) {
                    _updatePrefIsCustomAlarm(true);
                }

                // Refresh settings view
                FragmentTransaction tran = getFragmentManager().beginTransaction();
                tran.replace(R.id.settingsActivity_preferenceFragment, Settings_PreferenceFragment.newInstance());
                tran.commit();

                // Finish
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void _updatePrefIsCustomAlarm(boolean newValue) {
        // Update preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.pref_key_customAlarmEnabled), newValue);
        editor.apply();
    }
}

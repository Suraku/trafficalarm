package com.suraku.trafficalarm;

import android.content.Context;
import android.content.SharedPreferences;

import com.suraku.trafficalarm.activities.MainActivity;

/**
 *
 */

public class ApiCountdownRunnable implements Runnable
{
    private static final int API_TEST_TIMEOUT = 10;
    private int mApiCountdownRemaining;

    public ApiCountdownRunnable(MainActivity context) {
        retrieveLastTimestamp(context);
    }

    @Override
    public void run() {
        try {
            throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getApiCountdownRemaining() { return mApiCountdownRemaining; }
    public void setApiCountdownRemaining(int val) { mApiCountdownRemaining = val; }

    public void setCountdownEqualToTimeout(MainActivity context) {
        setApiCountdownRemaining(API_TEST_TIMEOUT);

        SharedPreferences preferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(context.getString(R.string.pref_key_contentFragment_testTimestamp), System.currentTimeMillis());

        editor.apply();
    }

    public void retrieveLastTimestamp(MainActivity context)
    {
        SharedPreferences preferences = context.getPreferences(Context.MODE_PRIVATE);
        Long currentTime = System.currentTimeMillis();

        Long msLastTimestamp = preferences.getLong(context.getString(R.string.pref_key_contentFragment_testTimestamp),
                currentTime - API_TEST_TIMEOUT * 1000);
        int secs = (int)(currentTime - msLastTimestamp) / 1000;

        if (secs < API_TEST_TIMEOUT) {
            this.setApiCountdownRemaining(API_TEST_TIMEOUT - secs);
        }
    }
}

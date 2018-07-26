package com.suraku.trafficalarm.activities;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.suraku.dev.components.filepicker.FilePicker_DialogFragment;
import com.suraku.dev.components.filepicker.IFilePickerResultListener;
import com.suraku.dev.components.filepicker.IFilePickerSelected;
import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.fragments.Settings_PreferenceFragment;

import java.io.File;

/**
 * User specific settings UI
 */
public class SettingsActivity extends BaseActivity implements
        IFilePickerResultListener
{
    private IFilePickerSelected mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FragmentTransaction tran = getFragmentManager().beginTransaction();
        tran.add(R.id.settingsActivity_preferenceFragment, Settings_PreferenceFragment.newInstance());
        tran.commit();
    }

    @Override
    public void setOnFilePickerSelectedListener(IFilePickerSelected listener) {
        mListener = listener;
    }

    @Override
    public void onDestroy() {
        mListener.onFileSelected(null);
        super.onDestroy();
    }

    @Override
    public void onClickFileSelected(File file, FilePicker_DialogFragment fragmentFilePicker)
    {
        // Check if file is audio
        boolean isAudio = true;
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(file.getPath());
            mp.prepare();
        } catch (Exception e) {
            isAudio = false;
        }

        if (!isAudio) {
            Helper.createScalableToast(this,
                    getString(R.string.value_settingsActivity_unableUseFileAudio),
                    Toast.LENGTH_LONG)
                .show();
            return;
        }

        // Save the value
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(getString(R.string.pref_key_alarmFilePath), file.getPath());
        editor.commit();


        // Update the view :- remove the picker, stop the audio, refresh the view
        fragmentFilePicker.dismiss();
        mListener.onFileSelected(file);

        FragmentTransaction tran = getFragmentManager().beginTransaction();
        tran.replace(R.id.settingsActivity_preferenceFragment, Settings_PreferenceFragment.newInstance());
        tran.commit();

        Logging.logEvent(this, getString(R.string.logging_settingsActivity_fileSelect), EventLevel.LOW);
    }
}

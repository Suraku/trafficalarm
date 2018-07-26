package com.suraku.trafficalarm.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.suraku.dev.components.filepicker.FilePicker_DialogFragment;
import com.suraku.dev.components.filepicker.IFilePickerResultListener;
import com.suraku.dev.components.filepicker.IFilePickerSelected;
import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.EventLevel;

import java.io.File;
import java.io.IOException;

/**
 * Preference with file picker
 */

public class AlarmAudioPreference extends android.preference.Preference implements
        IFilePickerSelected
{
    private Activity mContext;
    private MediaPlayer mMedia;

    public AlarmAudioPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (Activity) context;

        // Set listener
        ((IFilePickerResultListener)mContext).setOnFilePickerSelectedListener(this);

        setLayoutResource(R.layout.preference_alarm_audio);
    }

    @Override
    public void onFileSelected(File file) {
        stopMedia();
    }

    public void stopMedia() {
        if (mMedia != null) {
            mMedia.stop();
            mMedia.release();
            mMedia = null;
        }
    }

    @Override
    protected void onBindView(View rootView) {
        super.onBindView(rootView);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String filepath = preferences.getString(mContext.getString(R.string.pref_key_alarmFilePath), "");

        final File audioPrefFile;
        if (!Helper.isNullOrEmpty(filepath)) {
            File tmp_audioPrefFile = new File(filepath);
            if (tmp_audioPrefFile.exists()) {
                audioPrefFile = tmp_audioPrefFile;
            } else {
                audioPrefFile = null;
            }
        } else {
            audioPrefFile = null;
        }

        TextView audioFilename = (TextView) rootView.findViewById(R.id.preference_alarmFileName);
        final ImageView alarmPlayMedia = (ImageView) rootView.findViewById(R.id.preference_alarmTestPlayButton);

        if (audioPrefFile != null) {
            audioFilename.setText(audioPrefFile.getName());
        }

        audioFilename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display file picker
                FilePicker_DialogFragment fragment = FilePicker_DialogFragment.newInstance(mContext);
                fragment.show(mContext.getFragmentManager(), "AlarmFilePicker");
            }
        });

        alarmPlayMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPrefFile == null) {
                    return;
                }
                try {
                    if (mMedia == null) {
                        mMedia = new MediaPlayer();
                        mMedia.setAudioStreamType(AudioManager.STREAM_ALARM);   // Uses alarm volume level
                    }
                    if (mMedia.isPlaying()) {
                        alarmPlayMedia.setImageResource(R.drawable.ic_fileselectplay);
                        mMedia.reset();
                    } else {
                        alarmPlayMedia.setImageResource(R.drawable.ic_fileselectstop);
                        mMedia.setDataSource(audioPrefFile.getPath());
                        mMedia.prepare();
                        mMedia.start();
                    }

                } catch (IOException e) {
                    Logging.logEvent(mContext, mContext.getString(R.string.logging_alarmActivity_mediaPlayError),
                            EventLevel.HIGH, e);
                }
            }
        });
    }
}

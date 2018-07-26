package com.suraku.trafficalarm.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.EventLevel;

/**
 * Preference with numerical slider
 */

public class ThresholdDurationPreference extends DialogPreference implements
        DialogInterface.OnClickListener
{
    private int mDefaultValue = 1;
    private int mCurrentValue;
    private int mNewValue;

    public ThresholdDurationPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setDialogLayoutResource(R.layout.preference_threshold_slider);
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.preference_thresholdSeekbar);
        seekBar.setProgress(mCurrentValue);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (which == DialogInterface.BUTTON_POSITIVE) {
            SeekBar seekBar = (SeekBar) ((AlertDialog)dialog).findViewById(R.id.preference_thresholdSeekbar);
            mNewValue = seekBar.getProgress();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            mCurrentValue = mNewValue;
            persistInt(mNewValue);

            Logging.logEvent(getContext(), getContext().getString(R.string.logging_preferenceFrag_thresholdChanged), EventLevel.LOW);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mCurrentValue = this.getPersistedInt(mDefaultValue);
        } else {
            // Set default state from the XML attribute
            mCurrentValue = (Integer) defaultValue;
            persistInt(mCurrentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, mDefaultValue);
    }
}

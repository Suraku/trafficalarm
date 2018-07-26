package com.suraku.trafficalarm.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import com.suraku.trafficalarm.R;

/**
 * Displays a dialog for time input
 */
public class TimePicker_DialogFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener
{
    private TimePickerResultListener m_TimePickerResultListener;

    /* Empty constructor required when framework re-creates the fragment */
    public TimePicker_DialogFragment() { }

    public static TimePicker_DialogFragment newInstance(Context context, int hour, int minute)
    {
        TimePicker_DialogFragment fragment = new TimePicker_DialogFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(context.getString(R.string.key_timePickerFragment_hour), hour);
        bundle.putInt(context.getString(R.string.key_timePickerFragment_minute), minute);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Current time to be set as default
        int hour = 0, minute = 0;

        if (getArguments() != null) {
            hour = getArguments().getInt(getString(R.string.key_timePickerFragment_hour), 0);
            minute = getArguments().getInt(getString(R.string.key_timePickerFragment_minute), 0);
        }

        return new TimePickerDialog(getActivity(), this, hour, minute, true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        if (m_TimePickerResultListener != null) {
            m_TimePickerResultListener.onTimePickerResult(getArguments(), hourOfDay, minute);
        }
    }

    /** Class constructor/destructor initializers **/

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof TimePickerResultListener) {
            m_TimePickerResultListener = (TimePickerResultListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TimePickerResultListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_TimePickerResultListener = null;
    }

    /**
     * Allows communication back to the context which requested this fragment
     */
    public interface TimePickerResultListener
    {
        void onTimePickerResult(Bundle args, int hour, int minute);
    }
}

package com.suraku.dev.components.filepicker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.suraku.trafficalarm.R;

import java.io.File;

/**
 * Displays file-system folders/files
 */

public class FilePicker_DialogFragment extends DialogFragment
{
    private IFilePickerResultListener mListener;
    private RecyclerView m_recyclerView;

    /* Empty constructor required when framework re-creates the fragment */
    public FilePicker_DialogFragment() { }

    public static FilePicker_DialogFragment newInstance(Context context)
    {
        FilePicker_DialogFragment fragment = new FilePicker_DialogFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Choose file");
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_filepicker, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            m_recyclerView = (RecyclerView) view;

            m_recyclerView.setLayoutManager(new LinearLayoutManager(context));
            m_recyclerView.setAdapter(new FilePicker_RecyclerViewAdapter(this));
        }
        return view;
    }

    public IFilePickerResultListener getListener() { return mListener; }

    /** Class constructor/destructor initializers **/

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof IFilePickerResultListener) {
            mListener = (IFilePickerResultListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IFilePickerResultListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

}

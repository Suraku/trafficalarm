package com.suraku.trafficalarm.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.suraku.trafficalarm.R;

/**
 * Further information
 */

public class MainView_ExtraFragment extends Fragment
{
    public MainView_ExtraFragment() { }

    public static MainView_ExtraFragment newInstance(Context context) {
        return new MainView_ExtraFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mainview_extra, container, false);
    }
}

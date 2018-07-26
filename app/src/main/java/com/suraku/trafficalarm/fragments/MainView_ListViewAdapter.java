package com.suraku.trafficalarm.fragments;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.viewmodels.ImageWithText;

import java.util.ArrayList;

/**
 *
 */

public class MainView_ListViewAdapter extends ArrayAdapter<ImageWithText>
{
    public MainView_ListViewAdapter(Context context, ArrayList<ImageWithText> data) {
        super(context, 0, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageWithText item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.adapter_general_icontext_listitem, parent, false
            );
        }
        // Defaults
        convertView.setId(item.getId());

        ImageView icon = (ImageView) convertView.findViewById(R.id.adapterFile_iconTextGeneral_icon);
        TextView name = (TextView) convertView.findViewById(R.id.adapterFile_iconTextGeneral_name);

        // Icon
        icon.setImageResource(item.getIcon());

        // Text
        float fontSize = getContext().getResources().getDimension(R.dimen.font_size_small_1);
        name.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        name.setText(item.getText());

        return convertView;
    }
}

package com.suraku.trafficalarm.viewmodels;

import android.view.View;

/**
 *
 */

public class ImageWithText
{
    public ImageWithText(String text, int icon)
    {
        setText(text);
        setIcon(icon);
        setId(View.generateViewId());
    }

    public ImageWithText(String text, int icon, int id)
    {
        this(text, icon);
        setId(id);
    }

    private int id;
    private String text;
    private int icon;

    public int getId() { return id; }
    public void setId(int val) { this.id = val; }

    public String getText() { return text; }
    public void setText(String val) { this.text = val; }

    public int getIcon() { return icon; }
    public void setIcon(int val) { this.icon = val; }
}

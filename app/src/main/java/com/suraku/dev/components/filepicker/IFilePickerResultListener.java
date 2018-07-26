package com.suraku.dev.components.filepicker;

import java.io.File;

/**
 * Allows communication back to the context which requested this fragment
 * Bridge between adapter (trigger) and activity (resolver)
 */
public interface IFilePickerResultListener
{
    void onClickFileSelected(File file, FilePicker_DialogFragment fragment);

    void setOnFilePickerSelectedListener(IFilePickerSelected listener);
}

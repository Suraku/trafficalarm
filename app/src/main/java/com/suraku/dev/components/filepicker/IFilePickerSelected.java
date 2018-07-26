package com.suraku.dev.components.filepicker;

import java.io.File;

/**
 * Initializes the connection from the Preference to the Activity, then upon action
 * trigger within Activity, will relay information back to the Preference.
 */
public interface IFilePickerSelected
{
    void onFileSelected(File file);
}

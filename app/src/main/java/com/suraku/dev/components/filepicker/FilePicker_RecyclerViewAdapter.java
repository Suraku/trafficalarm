package com.suraku.dev.components.filepicker;

import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.suraku.trafficalarm.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * File list view
 */

public class FilePicker_RecyclerViewAdapter
        extends RecyclerView.Adapter<FilePicker_RecyclerViewAdapter.ViewHolder>
{
    /* Key (full filepath) : Value (file data) pairs */
    private List<FileItem> mCurrentFiles = new ArrayList<>();

    private final FilePicker_DialogFragment mContext;
    private File internalStorage = Environment.getExternalStorageDirectory();
    private File sdCardStorage = null;

    class FileItem implements Comparable<FileItem>
    {
        public FileItem(File file) {
            this.file = file;
            this.name = file.getName();

            if (file.isDirectory()) {
                this.icon = R.drawable.ic_folderdown;
            } else {
                // Check if file is audio
                ArrayList<String> fileTypes = new ArrayList<>(Arrays.asList(
                        "flac", "mp3", "wav", "wma", "webm"
                ));
                String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
                if (fileTypes.contains(extension)) {
                    this.icon = R.drawable.ic_fileselectplay;
                } else {
                    this.icon = R.drawable.ic_fileitem;
                }
            }
        }

        public File file;
        public String name;
        public int icon;

        @Override
        public int compareTo(FileItem item) {
            return this.name.toLowerCase().compareTo(item.name.toLowerCase());
        }
    }

    public FilePicker_RecyclerViewAdapter(FilePicker_DialogFragment context)
    {
        mContext = context;
        mCurrentFiles = getChildFiles(null);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.adapter_general_icontext_listitem, parent, false
        );
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        FileItem item = mCurrentFiles.toArray(new FileItem[mCurrentFiles.size()])[position];
        holder.mItem = item;
        holder.icon.setImageResource(item.icon);
        holder.filename.setText(item.name);
    }

    @Override
    public int getItemCount() { return mCurrentFiles.size(); }

    /** Private methods **/

    private ArrayList<FileItem> _getFiles(List<FileItem> files, Boolean isDirectory) 
    {
        ArrayList<FileItem> ret = new ArrayList<>();
        for (FileItem fileItem : files) {
            if (isDirectory == null && fileItem.file == null) {
                ret.add(fileItem);
            }
            if (fileItem.file != null && isDirectory != null && isDirectory == fileItem.file.isDirectory()) {
                ret.add(fileItem);
            }
        }
        return ret;
    }

    private List<FileItem> getChildFiles(File parentDirectory)
    {
        List<FileItem> allFiles = new ArrayList<>();

        if (parentDirectory == null) {
            FileItem internal = new FileItem(internalStorage);
            internal.name = "Internal Storage";
            allFiles.add(internal);

            File storageRoot = new File("/storage");
            if (storageRoot.exists() && storageRoot.listFiles().length > 0) {
                sdCardStorage = storageRoot.listFiles()[0];

                FileItem external = new FileItem(sdCardStorage);
                external.name = "SD Card";
                allFiles.add(external);
            }
        } else {
            FileItem upNav = new FileItem(parentDirectory.getParentFile());
            if (parentDirectory.equals(internalStorage) || parentDirectory.equals(sdCardStorage)) {
                upNav.file = null;
            }

            upNav.name = mContext.getString(R.string.value_ellipsis);
            upNav.icon = R.drawable.ic_folderup;
            allFiles.add(upNav);

            for (File file : parentDirectory.listFiles()) {
                allFiles.add(new FileItem(file));
            }
        }

        // Order list
        ArrayList<FileItem> directoryFiles = _getFiles(allFiles, true);
        ArrayList<FileItem> mediaFiles = _getFiles(allFiles, false);
        ArrayList<FileItem> nullFiles = _getFiles(allFiles, null);

        Collections.sort(directoryFiles);
        Collections.sort(mediaFiles);
        Collections.sort(nullFiles);

        ArrayList<FileItem> ret = new ArrayList<>();
        ret.addAll(nullFiles);
        ret.addAll(directoryFiles);
        ret.addAll(mediaFiles);

        return ret;
    }

    /** List item holder **/

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView icon;
        public final TextView filename;
        public FileItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            icon = (ImageView) view.findViewById(R.id.adapterFile_iconTextGeneral_icon);
            filename = (TextView) view.findViewById(R.id.adapterFile_iconTextGeneral_name);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItem.file == null || mItem.file.isDirectory()) {
                        mCurrentFiles = getChildFiles(mItem.file);
                        notifyDataSetChanged();
                    } else {
                        IFilePickerResultListener listener = mContext.getListener();
                        if (listener != null) {
                            listener.onClickFileSelected(mItem.file, mContext);
                        }
                    }
                }
            });
        }
    }
}

package com.suraku.trafficalarm.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.IListGeneral;
import com.suraku.trafficalarm.models.Event;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a list and makes a call to the
 * specified {@link EventItem_ListFragment.EventItemResultListener}.
 */

public class EventItem_RecyclerViewAdapter
    extends RecyclerView.Adapter<EventItem_RecyclerViewAdapter.ViewHolder>
    implements IListGeneral
{
    private List<Event> mValues;
    private final EventItem_ListFragment.EventItemResultListener mListener;

    public EventItem_RecyclerViewAdapter(List<Event> items, EventItem_ListFragment.EventItemResultListener listener)
    {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.adapter_event_listitem, parent, false
        );
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        holder.mItem = mValues.get(position);

        int icon;
        switch (holder.mItem.getEventLevel()) {
            case DEBUG:
                icon = R.drawable.ic_bug_debug;
                break;
            case ERROR:
                icon = R.drawable.ic_bug_error;
                break;
            case LOW:
                icon = R.drawable.ic_caution_sign_low;
                break;
            case MED:
                icon = R.drawable.ic_caution_sign_medium;
                break;
            case HIGH:
                icon = R.drawable.ic_caution_sign_high;
                break;
            default:
                icon = R.drawable.ic_information;
                break;
        }

        holder.levelIcon.setImageResource(icon);
        holder.date.setText(Helper.getDateFormatted(holder.mItem.getCreatedDate()));
        holder.time.setText(Helper.getTimeFormatted(holder.mItem.getCreatedDate(), false));
        holder.message.setText(holder.mItem.getDisplayMessage());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onClickEventItemFragment(holder.mItem);
                }
            }
        });
    }


    /** Implement Interfaces **/

    //TODO interface signature
    public void setNewList(List<Event> newList) {
        mValues = newList;
        this.notifyDataSetChanged();
    }

    @Override
    public void insertAtPosition(Object model, int pos) {
        mValues.add(pos, (Event) model);
        this.notifyItemInserted(pos);
    }

    @Override
    public void removeAtPosition(int pos) {
        mValues.remove(pos);
        this.notifyItemRemoved(pos);
    }

    @Override
    public int findItemPosition(Object modelUntyped) { //TODO reflection
        Event model = (Event) modelUntyped;
        for (int i = 0; i < mValues.size(); i++) {
            Event item = mValues.get(i);
            if (item.getEventPK().equals(model.getEventPK())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    /** List item creation **/

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView levelIcon;
        public final TextView date;
        public final TextView time;
        public final TextView message;
        public Event mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            levelIcon = (ImageView) view.findViewById(R.id.adapterEventItem_levelIcon);
            date = (TextView) view.findViewById(R.id.adapterEventItem_date);
            time = (TextView) view.findViewById(R.id.adapterEventItem_time);
            message = (TextView) view.findViewById(R.id.adapterEventItem_message);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + date.getText() + " " + message.getText() + "'";
        }
    }
}

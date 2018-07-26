package com.suraku.trafficalarm.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.IListGeneral;
import com.suraku.trafficalarm.models.TimeRequest;

import java.util.List;

import static com.suraku.trafficalarm.ApiHttp.formatTrafficDuration;

/**
 * {@link RecyclerView.Adapter} that can display a list and makes a call to the
 * specified {@link RecentItem_ListFragment.RecentTimeResultListener}.
 */
public class RecentItem_RecyclerViewAdapter
        extends RecyclerView.Adapter<RecentItem_RecyclerViewAdapter.ViewHolder>
        implements IListGeneral
{
    private List<TimeRequest> mValues;
    private final RecentItem_ListFragment.RecentTimeResultListener mListener;

    public RecentItem_RecyclerViewAdapter(List<TimeRequest> items, RecentItem_ListFragment.RecentTimeResultListener listener)
    {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.adapter_timerequest_listitem, parent, false
        );
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        holder.mItem = mValues.get(position);

        int totalSeconds = holder.mItem.getDurationInTraffic();
        String dateFormatted = Helper.getDateTimeFormatted(holder.mItem.getCreatedDate(), false);

        holder.firstColumn.setText(formatTrafficDuration(totalSeconds, 0));
        holder.secondColumn.setText(dateFormatted);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickRecentItemFragment(holder.mItem);
                }
            }
        });
    }


    /** Implement Interfaces **/

    @Override
    public void insertAtPosition(Object model, int pos) {
        mValues.add(pos, (TimeRequest) model);
        this.notifyItemInserted(pos);
    }

    @Override
    public void removeAtPosition(int pos) {
        mValues.remove(pos);
        this.notifyItemRemoved(pos);
    }

    @Override
    public int findItemPosition(Object modelUntyped) { //TODO reflection
        TimeRequest model = (TimeRequest) modelUntyped;
        for (int i = 0; i < mValues.size(); i++) {
            TimeRequest item = mValues.get(i);
            if (item.getTimeRequestPK().equals(model.getTimeRequestPK())) {
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
        public final TextView firstColumn;
        public final TextView secondColumn;
        public TimeRequest mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            firstColumn = (TextView) view.findViewById(R.id.adapterTimeRequest_firstItem);
            secondColumn = (TextView) view.findViewById(R.id.adapterTimeRequest_secondItem);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + firstColumn.getText() + "'";
        }
    }
}

package com.suraku.trafficalarm.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.IListGeneral;
import com.suraku.trafficalarm.models.Address;

import java.util.List;

/**
 * Address list view
 */

public class AddressPicker_RecyclerViewAdapter
        extends RecyclerView.Adapter<AddressPicker_RecyclerViewAdapter.ViewHolder>
        implements IListGeneral
{
    private String mTag = "";
    private List<Address> mValues;
    private AddressPicker_DialogFragment mContext;
    private final AddressPicker_DialogFragment.AddressPickerResultListener mListener;

    public AddressPicker_RecyclerViewAdapter(List<Address> items, AddressPicker_DialogFragment context)
    {
        mValues = items;
        mContext = context;
        mTag = context.getTag();
        mListener = context.getListener();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.adapter_general_text_listitem, parent, false
        );
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        holder.mItem = mValues.get(position);
        holder.firstColumn.setText(mValues.get(position).toString());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickAddressPickerFragment(holder.mItem, mTag);
                    mContext.dismiss();
                }
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mListener != null) {
                    return mListener.onLongClickAddressPickerFragment(holder.mItem, mContext);
                }
                return false;
            }
        });
    }

    /** Implement Interfaces **/

    @Override
    public void insertAtPosition(Object model, int pos) {
        mValues.add(pos, (Address)model);
        this.notifyItemInserted(pos);
    }

    @Override
    public void removeAtPosition(int pos) {
        mValues.remove(pos);
        this.notifyItemRemoved(pos);
    }

    @Override
    public int findItemPosition(Object modelUntyped) {
        Address model = (Address)modelUntyped;
        for (int i = 0; i < mValues.size(); i++) {
            Address item = mValues.get(i);
            if (item.getAddressPK().equals(model.getAddressPK())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() { return mValues.size(); }


    /** List item creation **/

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView firstColumn;
        public Address mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            firstColumn = (TextView) view.findViewById(R.id.adapterGeneral_firstItem);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + firstColumn.getText() + "'";
        }
    }
}

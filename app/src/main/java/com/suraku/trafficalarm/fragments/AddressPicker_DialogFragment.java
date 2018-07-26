package com.suraku.trafficalarm.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.ListMethod;
import com.suraku.trafficalarm.data.storage.DataStorageFactory;
import com.suraku.trafficalarm.data.storage.ILocalStorageProvider;
import com.suraku.trafficalarm.models.Address;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Displays user-entered addresses
 */

public class AddressPicker_DialogFragment extends DialogFragment
{
    private AddressPickerResultListener mListener;
    private RecyclerView m_recyclerView;
    private List<UUID> mIgnoredAddressKeys = new ArrayList<>();

    /* Empty constructor required when framework re-creates the fragment */
    public AddressPicker_DialogFragment() { }

    public static AddressPicker_DialogFragment newInstance(Context context, UUID[] ignoredAddressPKs)
    {
        AddressPicker_DialogFragment fragment = new AddressPicker_DialogFragment();

        Bundle bundle = new Bundle();
        ArrayList<String> tmpList = new ArrayList<>();

        if (ignoredAddressPKs != null) {
            for (UUID key : ignoredAddressPKs) {
                if (key != null) {
                    tmpList.add(key.toString());
                }
            }
        }

        bundle.putStringArrayList(context.getString(
                R.string.key_addressFragment_ignoredKeys), tmpList);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            return;
        }

        ArrayList<String> ignoredKeys = getArguments().getStringArrayList(
                getString(R.string.key_addressFragment_ignoredKeys));
        if (ignoredKeys == null) {
            return;
        }

        ArrayList<UUID> tmpList = new ArrayList<>();
        for (String key : ignoredKeys) {
            tmpList.add(UUID.fromString(key));
        }
        mIgnoredAddressKeys = tmpList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_addresspicker, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            m_recyclerView = (RecyclerView) view;
            m_recyclerView.setLayoutManager(new LinearLayoutManager(context));

            @SuppressWarnings("unchecked")
            ILocalStorageProvider<Address> repository = DataStorageFactory.getProvider(getContext(), Address.class);

            List<Pair<String, Object>> pairs = new ArrayList<>();
            for (UUID key : mIgnoredAddressKeys) {
                pairs.add(new Pair<String, Object>("AddressPK != ?", key));
            }

            List<Address> addressList = repository.findAll(pairs);
            Collections.sort(addressList);

            m_recyclerView.setAdapter(new AddressPicker_RecyclerViewAdapter(addressList, this));
        }
        return view;
    }

    public AddressPickerResultListener getListener() { return mListener; }

    public <T> T executeListMethod(ListMethod method, Class<T> returnType, Object... params)
    {
        RecyclerView.Adapter adapter = m_recyclerView.getAdapter();
        if (!(adapter instanceof AddressPicker_RecyclerViewAdapter)) {
            return null;
        }

        AddressPicker_RecyclerViewAdapter addressList = (AddressPicker_RecyclerViewAdapter)adapter;
        switch (method) {
            case FIND_ITEM_POSITION:
                return returnType.cast(addressList.findItemPosition(params[0]));
            case INSERT_AT_POSITION:
                addressList.insertAtPosition( params[0], Integer.class.cast(params[1]) );
                return null;
            case REMOVE_AT_POSITION:
                addressList.removeAtPosition( Integer.class.cast(params[0]) );
                return null;
            default:
                return null;
        }
    }

    /** Class constructor/destructor initializers **/

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof AddressPickerResultListener) {
            mListener = (AddressPickerResultListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AddressPickerResultListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    /**
     * Allows communication back to the context which requested this fragment
     * Bridge between adapter (trigger) and activity (resolver)
     */
    public interface AddressPickerResultListener
    {
        void onClickAddressPickerFragment(Address item, String tag);

        boolean onLongClickAddressPickerFragment(Address item, AddressPicker_DialogFragment fragment);
    }
}

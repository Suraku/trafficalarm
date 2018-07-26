package com.suraku.trafficalarm.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.suraku.trafficalarm.data.storage.DataStorageFactory;
import com.suraku.trafficalarm.data.storage.ILocalStorageProvider;
import com.suraku.trafficalarm.models.Address;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.models.TimeRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Displays the latest time requests for the currently selected address pairs
 */
public class RecentItem_ListFragment extends Fragment
{
    private RecentTimeResultListener mListener;
    private RecyclerView mRecyclerView;
    private UUID mAddressOriginPK;
    private UUID mAddressDestinationPK;

    /* Empty constructor required when framework re-creates the fragment */
    public RecentItem_ListFragment() { }

    /**
     * Should be used for manual instantiation, whereby any parameters are stored into the bundle.
     */
    public static RecentItem_ListFragment newInstance(Context context, Address origin, Address destination) {
        RecentItem_ListFragment fragment = new RecentItem_ListFragment();

        if (origin != null && destination != null) {
            Bundle bundle = new Bundle();

            bundle.putString(context.getString(R.string.key_activeAlarm_addressOriginPK), origin.getAddressPK().toString());
            bundle.putString(context.getString(R.string.key_activeAlarm_addressDestinationPK), destination.getAddressPK().toString());

            fragment.setArguments(bundle);
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            return;
        }

        String originPK = getArguments().getString(getContext().getString(R.string.key_activeAlarm_addressOriginPK), null);
        String destinationPK = getArguments().getString(getContext().getString(R.string.key_activeAlarm_addressDestinationPK), null);

        if (originPK != null && destinationPK != null) {
            mAddressOriginPK = UUID.fromString(originPK);
            mAddressDestinationPK = UUID.fromString(destinationPK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_recenttimes, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            @SuppressWarnings("unchecked")
            ILocalStorageProvider<TimeRequest> repository = DataStorageFactory.getProvider(getContext(), TimeRequest.class);

            List<TimeRequest> list = new ArrayList<>();
            if (mAddressOriginPK != null && mAddressDestinationPK != null) {
                list = repository.findAll(
                        new Pair<String, Object>("OriginAddressFK", mAddressOriginPK),
                        new Pair<String, Object>("DestinationAddressFK", mAddressDestinationPK)
                );
            }
            Collections.sort(list);
            if (list.size() > 10) {
                list = list.subList(0, 10);
            }

            mRecyclerView.setAdapter(new RecentItem_RecyclerViewAdapter(list, mListener));
        }
        return view;
    }

    /** Public methods **/

    public void addNewTimeRequest(TimeRequest model)
    {
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        RecentItem_RecyclerViewAdapter list = (RecentItem_RecyclerViewAdapter) adapter;

        list.insertAtPosition(model, 0);
        mRecyclerView.smoothScrollToPosition(0);
    }


    /** Class constructor/destructor initializers **/

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof RecentTimeResultListener) {
            mListener = (RecentTimeResultListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement RecentTimeResultListener");
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
     */
    public interface RecentTimeResultListener
    {
        // Triggered in the adapter
        void onClickRecentItemFragment(TimeRequest item);
    }
}

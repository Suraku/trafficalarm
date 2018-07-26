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
import android.widget.CompoundButton;
import android.widget.Switch;

import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.storage.DataStorageFactory;
import com.suraku.trafficalarm.data.storage.ILocalStorageProvider;
import com.suraku.trafficalarm.models.Event;

import java.util.Collections;
import java.util.List;

/**
 * Displays logged events for viewing of historic action data
 */

public class EventItem_ListFragment extends Fragment
{
    private EventItemResultListener mListener;
    private RecyclerView mRecyclerView;

    /* Empty constructor required when framework re-creates the fragment */
    public EventItem_ListFragment() { }

    /**
     * Should be used for manual instantiation, whereby any parameters are stored into the bundle.
     */
    public static EventItem_ListFragment newInstance(Context context) {
        return new EventItem_ListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_eventlist, container, false);
        View listView = rootView.findViewById(R.id.listView_eventList);

        // Set adapter
        if (listView instanceof RecyclerView) {
            Context context = listView.getContext();
            mRecyclerView = (RecyclerView) listView;
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            ILocalStorageProvider<Event> repository = DataStorageFactory.getProvider(context, Event.class);

            List<Event> list = repository.findAll(new Pair<String, Object>("IsVisible", 1));
            Collections.sort(list);

            if (list.size() > 200) {
                list = list.subList(0, 200);
            }

            mRecyclerView.setAdapter(new EventItem_RecyclerViewAdapter(list, mListener));
        }

        // Event switch
        Switch eventSwitch = (Switch) rootView.findViewById(R.id.eventDebugSwitch);
        eventSwitch.setChecked(true);
        eventSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
                EventItem_RecyclerViewAdapter adapterList = (EventItem_RecyclerViewAdapter) adapter;

                ILocalStorageProvider<Event> repository = DataStorageFactory.getProvider(getContext(), Event.class);
                List<Event> list;

                if (isChecked) {
                    list = repository.findAll(new Pair<String, Object>("IsVisible", 1));
                } else {
                    list = repository.findAll();
                }

                Collections.sort(list);
                if (list.size() > 200) {
                    list = list.subList(0, 200);
                }
                adapterList.setNewList(list);
            }
        });

        return rootView;
    }

    /** Public methods **/

    public void addNewEventItem(Event model)
    {
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        EventItem_RecyclerViewAdapter list = (EventItem_RecyclerViewAdapter) adapter;

        list.insertAtPosition(model, 0);
        mRecyclerView.smoothScrollToPosition(0);
    }


    /** Class constructor/destructor initializers **/

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof EventItemResultListener) {
            mListener = (EventItemResultListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement EventItemResultListener");
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
    public interface EventItemResultListener
    {
        // Triggered in the adapter
        void onClickEventItemFragment(Event item);
    }
}

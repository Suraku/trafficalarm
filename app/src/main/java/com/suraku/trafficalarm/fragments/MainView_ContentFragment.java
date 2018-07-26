package com.suraku.trafficalarm.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.suraku.trafficalarm.ApiCountdownRunnable;
import com.suraku.trafficalarm.ApiHttp;
import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.activities.AddressActivity;
import com.suraku.trafficalarm.activities.MainActivity;
import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.data.storage.DataStorageFactory;
import com.suraku.trafficalarm.data.storage.ILocalStorageProvider;
import com.suraku.trafficalarm.databinding.FragmentMainviewContentBinding;
import com.suraku.trafficalarm.models.Address;
import com.suraku.trafficalarm.models.Alarm;
import com.suraku.trafficalarm.models.TimeRequest;
import com.suraku.trafficalarm.services.AlarmReceiver;
import com.suraku.trafficalarm.viewmodels.MainViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Alarm content view
 */

public class MainView_ContentFragment extends Fragment
{
    private MainActivity mContext;
    private MainViewModel Model;
    private AlarmReceiver mAlarm;

    private Handler mHandler;
    private ApiCountdownRunnable mHandlerTask;


    /** Constructor Methods **/

    public MainView_ContentFragment() { }

    public MainViewModel getModel() { return this.Model; }

    public static MainView_ContentFragment newInstance(Context context, UUID alarmPK) {
        MainView_ContentFragment fragment = new MainView_ContentFragment();

        Bundle bundle = new Bundle();
        bundle.putString(context.getString(R.string.key_contentFragment_alarmPK), alarmPK.toString());
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (MainActivity) getActivity();
        mAlarm = new AlarmReceiver();

        Logging.logDebugEvent(mContext, "MainView_ContentFragment_onCreate");

        // Initialize view model
        ILocalStorageProvider<Alarm> repository = DataStorageFactory.getProvider(mContext, Alarm.class);
        UUID alarmPK = UUID.fromString(
                getArguments().getString(getString(R.string.key_contentFragment_alarmPK), null)
        );

        Model = new MainViewModel(mContext, repository.find(alarmPK));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Model/View binding
        FragmentMainviewContentBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_mainview_content, container, false);
        Logging.logDebugEvent(mContext, "MainView_ContentFragment_onCreateView");

        final View view = binding.getRoot();
        binding.setModel(Model);

        // Dynamically update UI
        createTimeRequestFragment();
        _updateAlarmImage(view);
        _apiCountdownHandlerCreate(view);

        // Events
        TextView alarmTimeDisplay = (TextView) view.findViewById(R.id.mainFragment_alarmTimeDisplay);
        alarmTimeDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePicker_Click(v);
            }
        });

        TextView originAddress = (TextView) view.findViewById(R.id.mainFragment_originAddressDisplay);
        originAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginAddress_Click(v);
            }
        });
        originAddress.setOnLongClickListener(TextView_DisplayHintMessage());

        TextView destinationAddress = (TextView) view.findViewById(R.id.mainFragment_destAddressDisplay);
        destinationAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {DestinationAddress_Click(v);
            }
        });
        destinationAddress.setOnLongClickListener(TextView_DisplayHintMessage());

        Button setButton = (Button) view.findViewById(R.id.mainFragment_setAlarmBtn);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetAlarm_Click(v);
            }
        });

        Button testButton = (Button) view.findViewById(R.id.mainFragment_saveAlarmBtn);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiRequestTest_Click(v);
            }
        });

        // View
        return view;
    }

    @Override
    public void onDestroy() {
        Logging.logDebugEvent(mContext, "MainView_ContentFragment_onDestroy");
        _apiCountdownHandlerDestroy();
        super.onDestroy();
    }


    /** Activity Methods **/

    public void createTimeRequestFragment() {
        FragmentTransaction tran = getChildFragmentManager().beginTransaction();
        tran.replace(R.id.mainFragment_fragmentListRecentRequests, RecentItem_ListFragment.newInstance(
                mContext, Model.getAlarm().getOriginAddress(), Model.getAlarm().getDestinationAddress()
        ));
        tran.commit();
    }

    public void addNewTimeRequest(TimeRequest timeRequest) {
        // Model is NULL when "resuming" activity post-destroyed via Alarm
        if (Model == null) {
            Log.d("Suraku", "MODEL is NULL");
            return;
        }

        RecentItem_ListFragment listFragment = (RecentItem_ListFragment) getChildFragmentManager().findFragmentById(
                R.id.mainFragment_fragmentListRecentRequests);
        listFragment.addNewTimeRequest(timeRequest);
    }

    public void showAddressActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.key_addressActivity_title), getString(R.string.value_mainActivity_titleNewAddress));

        Intent intent = new Intent(mContext, AddressActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void clearPreferences(boolean ignoreAlarm) {
        if (!ignoreAlarm) {
            Model.getAlarm().setMinute(0);
            Model.getAlarm().setHour(0);
        }
        Model.getAlarm().setDestinationAddress(null);
        Model.getAlarm().setOriginAddress(null);

        if (mAlarm.isAlarmSet(mContext)) {
            mAlarm.cancelAlarm(mContext);
            _updateAlarmImage(getView());
        }

        updateAlarmInViewModel();
        createTimeRequestFragment();
    }

    public void updateAlarmInViewModel() {
        ILocalStorageProvider<Alarm> repository = DataStorageFactory.getProvider(mContext, Alarm.class);
        repository.update(Model.getAlarm());
    }

    public void cancelAlarm() {
        mAlarm.cancelAlarm(mContext);
        _updateAlarmImage(getView());
    }

    public boolean isAlarmSet() {
        return mAlarm.isAlarmSet(mContext);
    }


    /** Private Methods **/

    private void _updateAlarmImage(View rootView) {
        // Update image
        ImageView feedbackImg = (ImageView) rootView.findViewById(R.id.mainFragment_alarmSetStatus);
        if (mAlarm.isAlarmSet(mContext)) {
            feedbackImg.setImageResource(R.drawable.ic_greentick);
        } else {
            feedbackImg.setImageResource(R.drawable.ic_orangecircleline);
        }
    }

    /* Handler preferred over separate thread as our aim is to update the UI */
    private void _apiCountdownHandlerCreate(final View rootView) {
        Logging.logDebugEvent(mContext, "MainView_ContentFragment_apiCountdownHandlerCreate");
        if (mHandler == null) {
            mHandler = new Handler();
        }

        if (mHandlerTask == null) {
            mHandlerTask = new ApiCountdownRunnable(mContext) {
                @Override
                public void run() {
                    this.setApiCountdownRemaining(this.getApiCountdownRemaining() - 1);

                    TextView textView = (TextView) rootView.findViewById(R.id.mainFragment_alarmSavedStatusText);
                    ImageView imageView = (ImageView) rootView.findViewById(R.id.mainFragment_alarmSavedStatusImg);

                    // Stop once we're done
                    int secondsRemaining = this.getApiCountdownRemaining();
                    if (secondsRemaining <= 0) {
                        mHandler.removeCallbacks(mHandlerTask);

                        // Display image
                        textView.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        return;

                    } else {
                        // Display value
                        textView.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                    }

                    // Update the view
                    String text = Integer.toString(secondsRemaining);
                    if (secondsRemaining > 60) {
                        DecimalFormat df = new DecimalFormat("#");
                        text = df.format(Math.ceil((double)secondsRemaining / (double)60)) + "m";
                    }
                    textView.setText(text);

                    mHandler.postDelayed(mHandlerTask, 1000);
                }
            };
        }
        mHandlerTask.retrieveLastTimestamp(mContext);
        mHandlerTask.run();
    }

    private void _apiCountdownHandlerDestroy()
    {
        Logging.logDebugEvent(mContext, "MainView_ContentFragment_apiCountdownHandlerDestroy");
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    private void _showAddressListFragment(String fragmentTag)
    {
        @SuppressWarnings("unchecked")
        ILocalStorageProvider<Address> addressRepository = DataStorageFactory.getProvider(
                mContext, Address.class);

        // List of UUIDs
        List<Pair<String, Object>> ignoreKeys = new ArrayList<>();

        if (Model.getAlarm().getDestinationAddress() != null && !fragmentTag.equals(getString(R.string.tag_addressFragment_origin)))
            ignoreKeys.add(new Pair<String, Object>("AddressPK != ?", Model.getAlarm().getDestinationAddress().getAddressPK()));
        if (Model.getAlarm().getOriginAddress() != null && !fragmentTag.equals(getString(R.string.tag_addressFragment_destination)))
            ignoreKeys.add(new Pair<String, Object>("AddressPK != ?", Model.getAlarm().getOriginAddress().getAddressPK()));

        if (addressRepository.findAll(ignoreKeys).size() > 1) {
            UUID[] array = new UUID[ignoreKeys.size()];
            for (int i = 0; i < ignoreKeys.size(); i++) {
                array[i] = (UUID)ignoreKeys.get(i).second;
            }

            AddressPicker_DialogFragment fragment = AddressPicker_DialogFragment.newInstance(
                    mContext, array
            );
            fragment.show(getFragmentManager(), fragmentTag);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.value_mainActivity_tooFewAddressNotice));
        builder.setCancelable(true);

        builder.setPositiveButton(getString(R.string.value_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showAddressActivity();
            }
        });
        builder.setNeutralButton(getString(R.string.value_dialog_swap), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Address destination = Model.getAlarm().getDestinationAddress();
                Model.getAlarm().setDestinationAddress(Model.getAlarm().getOriginAddress());
                Model.getAlarm().setOriginAddress(destination);

                // Update time list
                updateAlarmInViewModel();
                createTimeRequestFragment();
            }
        });
        builder.setNegativeButton(getString(R.string.value_dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /** Events **/

    public void TimePicker_Click(View view) {
        TimePicker_DialogFragment picker = TimePicker_DialogFragment.newInstance(
                mContext, Model.getAlarm().getHour(), Model.getAlarm().getMinute());
        picker.show(getFragmentManager(), "timePicker");
    }

    public void DestinationAddress_Click(View view) {
        _showAddressListFragment(getString(R.string.tag_addressFragment_destination));
    }

    public void OriginAddress_Click(View view) {
        _showAddressListFragment(getString(R.string.tag_addressFragment_origin));
    }

    public void SetAlarm_Click(View view) {
        if (Model.getAlarm().getOriginAddress() == null || Model.getAlarm().getDestinationAddress() == null) {
            Helper.createScalableToast(mContext, getString(R.string.value_contentFragment_originDestRequired), Toast.LENGTH_SHORT).show();
            return;
        }

        // Set mAlarm
        if (mAlarm.isAlarmSet(mContext)) {
            mAlarm.cancelAlarm(mContext);
        } else {
            mAlarm.setAlarm(mContext, Model);
        }

        // Update image
        _updateAlarmImage(getView());
    }

    public void ApiRequestTest_Click(View view)
    {
        if (Model.getAlarm().getOriginAddress() == null || Model.getAlarm().getDestinationAddress() == null) {
            Helper.createScalableToast(mContext, getString(R.string.value_contentFragment_originDestRequired), Toast.LENGTH_SHORT).show();

        } else if (mHandlerTask.getApiCountdownRemaining() <= 0) {
            mHandlerTask.setCountdownEqualToTimeout(mContext);
            mHandlerTask.run();

            ApiHttp task = new ApiHttp(mContext, Model.getAlarm().getOriginAddress(), Model.getAlarm().getDestinationAddress());
            task.execute();

        } else {
            Helper.createScalableToast(mContext, getString(R.string.value_pleaseWait), Toast.LENGTH_SHORT).show();
        }
    }

    public View.OnLongClickListener TextView_DisplayHintMessage() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                TextView textView = (TextView) view;
                Helper.createScalableToast(mContext, textView.getHint().toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        };
    }

}

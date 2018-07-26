package com.suraku.trafficalarm.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.suraku.trafficalarm.ApiHttp;
import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.data.extensions.ListMethod;
import com.suraku.trafficalarm.data.storage.DataStorageFactory;
import com.suraku.trafficalarm.data.storage.ILocalStorageProvider;
import com.suraku.trafficalarm.fragments.AddressPicker_DialogFragment;
import com.suraku.trafficalarm.fragments.EventItem_ListFragment;
import com.suraku.trafficalarm.fragments.MainView_ContentFragment;

import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.fragments.MainView_ListViewAdapter;
import com.suraku.trafficalarm.fragments.RecentItem_ListFragment;
import com.suraku.trafficalarm.fragments.TimePicker_DialogFragment;
import com.suraku.trafficalarm.models.Address;
import com.suraku.trafficalarm.models.Alarm;
import com.suraku.trafficalarm.models.Event;
import com.suraku.trafficalarm.models.TimeRequest;
import com.suraku.trafficalarm.viewmodels.ApiGMapResult;
import com.suraku.trafficalarm.viewmodels.ImageWithText;
import com.suraku.trafficalarm.viewmodels.MainViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements
        TimePicker_DialogFragment.TimePickerResultListener,
        AddressPicker_DialogFragment.AddressPickerResultListener,
        RecentItem_ListFragment.RecentTimeResultListener,
        EventItem_ListFragment.EventItemResultListener,
        ApiHttp.GMapResultListener
{
    private MainActivity mContext;
    private MainActivityPageAdapter myPageAdapter;

    /** Content Page Swiping **/
    class MainActivityPageAdapter extends FragmentStatePagerAdapter
    {
        private List<Fragment> fragments;
        private boolean fragReplaced = true;

        public MainActivityPageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        public void setItem(Fragment fragment, int pos) {
            FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
            tran.remove(this.fragments.get(pos));
            tran.commit();

            myPageAdapter.fragments.set(pos, fragment);
            myPageAdapter.notifyDataSetChanged();
            fragReplaced = true;
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }

        @Override
        public int getItemPosition(Object object) {
            if (this.fragReplaced) {
                this.fragReplaced = false;
                return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
        }
    }

    /** Left List Drawer **/
    private ListView mLeftAlarmListView;
    private ListView mLeftGeneralListView;
    private MainView_ListViewAdapter mLeftAlarmListAdapter;
    private MainView_ListViewAdapter mLeftGeneralListAdapter;

    private DrawerLayout mDrawerLayout;
    private LinearLayout mLeftDrawer;
    private ActionBarDrawerToggle mDrawerToggle;


    /** Activity Setup **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mContext = this;

        // Initialize database
        Helper.getUser(mContext);
        Helper.getActiveAlarm(mContext);

        // Prepare content views
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(MainView_ContentFragment.newInstance(this, Helper.getActiveAlarm(mContext).getAlarmPK()));
        fragments.add(EventItem_ListFragment.newInstance(this));

        // Set content view
        myPageAdapter = new MainActivityPageAdapter(getSupportFragmentManager(), fragments);
        ViewPager viewPager = (ViewPager) findViewById(R.id.activityMain_viewPager);
        viewPager.setAdapter(myPageAdapter);
        viewPager.setCurrentItem(0);

        // Display menu icon
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        //TODO
        /**
         * Formatting: Add duration difference into timerequest list, and shorten secs to s and mins to m
         * Alarm: keep media playing when screen turns off
         * ApiRequest: Take into consideration for what happens when no internet for more than 3 minutes
         * ApiRequest: Loading modal (with cancel button
         * Clean up old TimeRequests when greater than 40MB
         * Implement "Share" feature
         * Implement "About" feature, short description/readme with any supporting URLs, and add the below policy notices
         * Create a Terms of Use - state that users are bound by Google's Terms of Service
         * Create a Privacy Policy - state the app uses Google's Maps API and reference Google's Privacy Policy
         * Display both above terms within the app itself, and on the apps download page on Play
         * DEVICES - Cover all common screen sizes for font dimensions
         * TESTING - Ensure it functions on various devices
         */

        // List Side View
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_title_menu, R.string.app_name);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mLeftDrawer = (LinearLayout) findViewById(R.id.mainActivity_listSideView);
        mLeftAlarmListView = (ListView) findViewById(R.id.mainActivity_listSide_alarmList);
        mLeftGeneralListView = (ListView) findViewById(R.id.mainActivity_listSide_generalList);

        // Populate list view contents
        _invalidateAlarmListDrawer();
        _invalidateGeneralListDrawer();

        mLeftAlarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                ILocalStorageProvider<Alarm> repository = DataStorageFactory.getProvider(mContext, Alarm.class);
                List<Alarm> alarms = repository.findAll();
                Collections.sort(alarms);

                // Check alarms
                final Alarm selectedAlarm = alarms.get(i);
                final Alarm originalAlarm = Helper.getActiveAlarm(mContext);

                if (selectedAlarm.getAlarmPK().equals(originalAlarm.getAlarmPK())) {
                    return;
                }

                MainView_ContentFragment fragment = _getContentFragment();
                boolean isAlarmSet = fragment.isAlarmSet();

                if (!isAlarmSet) {
                    _switchActiveAlarm(selectedAlarm, originalAlarm);
                    Logging.logEvent(mContext, getString(R.string.logging_activeAlarmSwitched), EventLevel.LOW);
                    return;
                }

                // Confirm change
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(getString(R.string.value_mainActivity_activeAlarmSwitching));
                builder.setCancelable(true);

                builder.setPositiveButton(getString(R.string.value_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        _switchActiveAlarm(selectedAlarm, originalAlarm);
                        Logging.logEvent(mContext, getString(R.string.logging_activeAlarmSwitched), EventLevel.LOW);
                    }
                });
                builder.setNegativeButton(getString(R.string.value_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        mLeftGeneralListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (view.getId() == R.id.mainActivity_listSideView_share) {
                    if (i == 1) {}; //TODO share and about + log event
                }
                if (view.getId() == R.id.mainActivity_listSideView_about) {

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Handle any extras
        if (getIntent().getExtras() != null) {
            String json = getIntent().getExtras().getString(getString(R.string.key_apiJsonResult), null);

            if (!Helper.isNullOrEmpty(json)) {
                Logging.logDebugEvent(mContext, "MainActivity_onResume - Saving json from intent.");

                // Save API request
                TimeRequest timeRequest = _saveJsonToTimeRequest(json);

                // Display changes
                MainView_ContentFragment fragment = _getContentFragment();
                fragment.addNewTimeRequest(timeRequest);

                // Clean up
                getIntent().removeExtra(getString(R.string.key_apiJsonResult));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);  // Always overwrite existing intent for if we have Extras we wish to add
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.action_addressAdd) {
            MainView_ContentFragment fragment = _getContentFragment();
            fragment.showAddressActivity();

        } else if (id == R.id.action_clearMainModel) {
            MainView_ContentFragment fragment = _getContentFragment();
            fragment.clearPreferences(false);
            _invalidateAlarmListDrawer();
            Logging.logEvent(mContext, getString(R.string.logging_mainActivity_contentCleared), EventLevel.LOW);
        }

        return super.onOptionsItemSelected(item);
    }


    /** Private Methods **/

    private MainView_ContentFragment _getContentFragment() {
        Logging.logDebugEvent(mContext, "MainActivity_getContentFragment");
        return (MainView_ContentFragment) myPageAdapter.getItem(0);
    }

    private void _invalidateAlarmListDrawer() {
        ArrayList<ImageWithText> list = new ArrayList<>();
        for (Alarm alarm : Helper.getAlarms(mContext)) {
            MainViewModel model = new MainViewModel(mContext, alarm);
            list.add(new ImageWithText(model.getAlarmTime(), R.drawable.ic_alarm_clock));
        }

        if (mLeftAlarmListAdapter == null) {
            mLeftAlarmListAdapter = new MainView_ListViewAdapter(mContext, list);
            mLeftAlarmListView.setAdapter(mLeftAlarmListAdapter);
        } else {
            mLeftAlarmListAdapter.clear();
            mLeftAlarmListAdapter.addAll(list);
        }
    }

    private void _invalidateGeneralListDrawer() {
        ArrayList<ImageWithText> list = new ArrayList<>();
        list.add(new ImageWithText(getString(R.string.value_share), R.drawable.ic_share, R.id.mainActivity_listSideView_share));
        list.add(new ImageWithText(getString(R.string.value_about), R.drawable.ic_information, R.id.mainActivity_listSideView_about));

        if (mLeftGeneralListAdapter == null) {
            mLeftGeneralListAdapter = new MainView_ListViewAdapter(mContext, list);
            mLeftGeneralListView.setAdapter(mLeftGeneralListAdapter);
        } else {
            mLeftGeneralListAdapter.clear();
            mLeftGeneralListAdapter.addAll(list);
        }
    }

    private TimeRequest _saveJsonToTimeRequest(String json) {
        Logging.logDebugEvent(mContext, "MainActivity_saveJsonToTimeRequest");

        @SuppressWarnings("unchecked")
        ILocalStorageProvider<TimeRequest> timeRepository = DataStorageFactory.getProvider(mContext, TimeRequest.class);

        // Save data
        Alarm activeAlarm = Helper.getActiveAlarm(mContext);
        TimeRequest timeRequest = new TimeRequest(
                activeAlarm.getOriginAddressFK(),
                activeAlarm.getDestinationAddressFK()
        );
        timeRequest.setJsonResponse(json);
        timeRequest.setDurationInTraffic(Helper.findJsonObject(Integer.class, json, ApiHttp.KEY_DURATION_TRAFFIC));
        timeRepository.update(timeRequest);

        return timeRequest;
    }

    private void _switchActiveAlarm(Alarm selectedAlarm, Alarm originalAlarm) {
        @SuppressWarnings("unchecked")
        ILocalStorageProvider<Alarm> repository = DataStorageFactory.getProvider(mContext, Alarm.class);

        // Update active alarm
        selectedAlarm.setIsActive(true);
        originalAlarm.setIsActive(false);

        if (repository.update(originalAlarm) > 0) {
            repository.update(selectedAlarm);
        }

        // Get the appropriate alarm
        MainView_ContentFragment contentFragment = MainView_ContentFragment.newInstance(
                mContext, selectedAlarm.getAlarmPK()
        );
        myPageAdapter.setItem(contentFragment, 0);
        contentFragment.cancelAlarm();

        // Finish up
        mDrawerLayout.closeDrawer(mLeftDrawer);
    }


    /** Implement Interfaces **/

    @Override
    public void onGMapsResult(ApiGMapResult result) {
        Logging.logDebugEvent(mContext, "MainActivity_onGMapsResult");

        // Error check
        if (!Helper.isNullOrEmpty(result.getErrorMessage())) {
            //TODO display message to user
        }

        // Save result
        MainView_ContentFragment fragment = _getContentFragment();
        TimeRequest timeRequest = _saveJsonToTimeRequest(result.getJSONString());

        // Display changes
        fragment.addNewTimeRequest(timeRequest);

        // Show alarm (with map)
        Intent alarmIntent = new Intent(this, AlarmActivity.class);
        alarmIntent.putExtra(getString(R.string.key_apiJsonResult), timeRequest.getJsonResponse());
        startActivity(alarmIntent);
    }

    @Override
    public void onTimePickerResult(Bundle args, int hour, int minute) {
        Logging.logEvent(mContext, getString(R.string.logging_newAlarmTimeSelected), EventLevel.LOW);

        MainView_ContentFragment fragment = _getContentFragment();
        MainViewModel model = fragment.getModel();

        // Update view
        model.getAlarm().setHour(hour);
        model.getAlarm().setMinute(minute);
        fragment.updateAlarmInViewModel();

        _invalidateAlarmListDrawer();
        fragment.cancelAlarm();
    }

    @Override
    public void onClickAddressPickerFragment(Address item, String tag) {
        Logging.logEvent(mContext, getString(R.string.logging_mainActivity_addressSelected), EventLevel.LOW);

        MainView_ContentFragment fragment = _getContentFragment();
        MainViewModel model = fragment.getModel();

        if (tag.equals(getString(R.string.tag_addressFragment_destination))) {
            if (Address.isEqual(model.getAlarm().getOriginAddress(), item)) {
                model.getAlarm().setOriginAddress(model.getAlarm().getDestinationAddress());
            }
            model.getAlarm().setDestinationAddress(item);
        }
        else if (tag.equals(getString(R.string.tag_addressFragment_origin))) {
            if (Address.isEqual(model.getAlarm().getDestinationAddress(), item)) {
                model.getAlarm().setDestinationAddress(model.getAlarm().getOriginAddress());
            }
            model.getAlarm().setOriginAddress(item);
        }
        fragment.updateAlarmInViewModel();

        // Update time list
        fragment.createTimeRequestFragment();
    }

    @Override
    public boolean onLongClickAddressPickerFragment(final Address item, final AddressPicker_DialogFragment addressPickerFragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.value_mainActivity_deleteAddressWarning));
        builder.setCancelable(true);

        builder.setPositiveButton(getString(R.string.value_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                @SuppressWarnings("unchecked")
                ILocalStorageProvider<Address> addressRepos = DataStorageFactory.getProvider(mContext, Address.class);

                if (addressRepos.delete(item) <= 0) {
                    return;
                }
                int position = addressPickerFragment.executeListMethod(ListMethod.FIND_ITEM_POSITION, Integer.class, item);
                addressPickerFragment.executeListMethod(ListMethod.REMOVE_AT_POSITION, null, position);

                // Update UI
                MainView_ContentFragment mainViewFragment = _getContentFragment();
                MainViewModel model = mainViewFragment.getModel();

                if (addressRepos.findAll().size() <= 2) {
                    addressPickerFragment.dismiss();
                }
                if (Address.isEqual(model.getAlarm().getDestinationAddress(), item) || Address.isEqual(model.getAlarm().getOriginAddress(), item)) {
                    mainViewFragment.clearPreferences(true);
                }
                Logging.logEvent(mContext, getString(R.string.logging_mainActivity_addressDeleted), EventLevel.LOW);
            }
        });
        builder.setNegativeButton(getString(R.string.value_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return true;
    }

    @Override
    public void onClickRecentItemFragment(TimeRequest item) {
        // Do nothing
    }

    @Override
    public void onClickEventItemFragment(Event item) {
        // Do nothing
    }

}

package com.suraku.trafficalarm.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.suraku.trafficalarm.BR;
import com.suraku.trafficalarm.data.storage.DataStorageFactory;
import com.suraku.trafficalarm.data.storage.ILocalStorageProvider;
import com.suraku.trafficalarm.models.Address;
import com.suraku.trafficalarm.models.Alarm;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * View model for main activity
 */

public class MainViewModel extends BaseObservable implements Alarm.IAlarmPropertyListener
{
    @Override
    public void OnPropertyChanged(Alarm.PropertyNames propertyName, Object value) {
        switch (propertyName) {
            case NAME_HOUR:
            case NAME_MINUTE:
                setAlarmTime();
                break;
            case NAME_ORIGIN_FK:
                setDisplayOriginAddress(value != null ? value.toString() : null);
                break;
            case NAME_DESTINATION_FK:
                setDisplayDestinationAddress(value != null ? value.toString() : null);
                break;
            default:
                break;
        }
    }


    public MainViewModel(Context context, Alarm alarm)
    {
        // Initialize
        this.alarm = alarm;
        this.alarm.setListener(this);

        // Setup view data
        @SuppressWarnings("unchecked")
        ILocalStorageProvider<Address> repository = DataStorageFactory.getProvider(context, Address.class);

        this.alarm.setOriginAddress(repository.find(alarm.getOriginAddressFK()));
        this.alarm.setDestinationAddress(repository.find(alarm.getDestinationAddressFK()));
        this.setAlarmTime();
    }

    private Alarm alarm;
    private String alarmTime;
    private String displayOriginAddress;
    private String displayDestinationAddress;


    // ~~ Getters & Setters ~~ \\

    public Alarm getAlarm() { return alarm; }

    @Bindable
    public String getAlarmTime() { return alarmTime; }
    private void setAlarmTime() {
        NumberFormat formatter = new DecimalFormat("00");
        String hourText = formatter.format(alarm.getHour());
        String minText = formatter.format(alarm.getMinute());

        this.alarmTime = hourText + ":" + minText;
        notifyPropertyChanged(BR.alarmTime);
    }

    @Bindable
    public String getDisplayOriginAddress() { return displayOriginAddress; }
    public void setDisplayOriginAddress(String val) {
        this.displayOriginAddress = val;
        notifyPropertyChanged(BR.displayOriginAddress);
    }

    @Bindable
    public String getDisplayDestinationAddress() { return displayDestinationAddress; }
    public void setDisplayDestinationAddress(String val) {
        displayDestinationAddress = val;
        notifyPropertyChanged(BR.displayDestinationAddress);
    }
}

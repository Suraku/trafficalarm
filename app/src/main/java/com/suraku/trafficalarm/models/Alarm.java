package com.suraku.trafficalarm.models;

import com.suraku.dev.sqlite.SQLiteModelMetadata;

import java.util.Calendar;
import java.util.UUID;

/**
 * Model
 */

public class Alarm implements Comparable<Alarm>
{
    /*
     * Default constructor for type instantiation
     */
    public Alarm()
    {
        this.alarmPK = UUID.randomUUID();
        this.hour = 0;
        this.minute = 0;
        this.isActive = 0;
        this.createdDate = Calendar.getInstance();
    }

    public Alarm(User user) {
        this();
        this.userFK = user.getUserPK();
    }

    @Override
    public int compareTo(Alarm item) {
        return this.createdDate.compareTo(item.createdDate);
    }

    private Address originAddress;
    private Address destinationAddress;

    @SQLiteModelMetadata
    private UUID alarmPK;

    @SQLiteModelMetadata
    private int hour;

    @SQLiteModelMetadata
    private int minute;

    @SQLiteModelMetadata
    private int isActive;

    @SQLiteModelMetadata
    private Calendar createdDate;

    @SQLiteModelMetadata
    private UUID userFK;

    @SQLiteModelMetadata
    private UUID originAddressFK;

    @SQLiteModelMetadata
    private UUID destinationAddressFK;


    // ~~ Getters & Setters ~~ \\

    public UUID getAlarmPK() { return alarmPK; }
    public void setAlarmPK(UUID val) { this.alarmPK = val; }

    public int getHour() { return hour; }
    public void setHour(int val) {
        this.hour = val;
        if (mListener != null) {
            mListener.OnPropertyChanged(PropertyNames.NAME_HOUR, val);
        }
    }

    public int getMinute() { return minute; }
    public void setMinute(int val) {
        this.minute = val;
        if (mListener != null) {
            mListener.OnPropertyChanged(PropertyNames.NAME_MINUTE, val);
        }
    }

    public boolean getIsActive() { return isActive != 0; }
    public void setIsActive(boolean val) { this.isActive = val ? 1 : 0; }

    public Calendar getCreatedDate() { return createdDate; }
    public void setCreatedDate(Calendar val) { this.createdDate = val; }

    public UUID getUserFK() { return userFK; }
    public void setUserFK(UUID val) { this.userFK = val; }

    public UUID getOriginAddressFK() { return originAddressFK; }
    public UUID getDestinationAddressFK() { return destinationAddressFK; }

    public Address getOriginAddress() { return originAddress; }
    public void setOriginAddress(Address val) {
        this.originAddress = val;

        if (val != null) {
            this.originAddressFK = val.getAddressPK();
        } else {
            this.originAddressFK = null;
        }

        if (mListener != null) {
            mListener.OnPropertyChanged(PropertyNames.NAME_ORIGIN_FK, val != null ? val.toString() : null);
        }
    }

    public Address getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(Address val) {
        this.destinationAddress = val;

        if (val != null) {
            this.destinationAddressFK = val.getAddressPK();
        } else {
            this.destinationAddressFK = null;
        }

        if (mListener != null) {
            mListener.OnPropertyChanged(PropertyNames.NAME_DESTINATION_FK, val != null ? val.toString() : null);
        }
    }


    /*
     * Listener to provide notifications of properties changing
     */
    private IAlarmPropertyListener mListener;

    public interface IAlarmPropertyListener {
        void OnPropertyChanged(PropertyNames propertyName, Object value);
    }
    public void setListener(IAlarmPropertyListener listener) {
        mListener = listener;
    }

    public enum PropertyNames
    {
        NAME_HOUR(0),
        NAME_MINUTE(1),
        NAME_ORIGIN_FK(2),
        NAME_DESTINATION_FK(3);

        private final int number;
        public int getNumber() { return number; }

        PropertyNames(int number) { this.number = number; }
    }
}

package com.suraku.trafficalarm.models;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.suraku.dev.sqlite.SQLiteModelMetadata;
import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.data.storage.DataStorageFactory;
import com.suraku.trafficalarm.data.storage.ILocalStorageProvider;

import java.util.Calendar;
import java.util.UUID;

/**
 * Model
 */

public class Address extends BaseObservable implements Comparable<Address>
{
    /*
     * Default empty constructor for type instantiation
     */
    public Address()
    {
        this.addressPK = UUID.randomUUID();
        this.addressLineOne = "";
        this.addressLineTwo = "";
        this.city = "";
        this.county = "";
        this.postcode = "";
        this.createdDate = Calendar.getInstance();
    }

    public Address(User user)
    {
        this();
        this.setUser(user);
    }

    @Override
    public int compareTo(Address item) {
        return this.toString().compareTo(item.toString());
    }

    private User user;

    @SQLiteModelMetadata
    private UUID addressPK;

    @SQLiteModelMetadata
    private String addressLineOne;

    @SQLiteModelMetadata
    private String addressLineTwo;

    @SQLiteModelMetadata
    private String city;

    @SQLiteModelMetadata
    private String county;

    @SQLiteModelMetadata
    private String postcode;

    @SQLiteModelMetadata
    private Calendar createdDate;

    @SQLiteModelMetadata
    private UUID userFK;


    // ~~ Getters & Setters ~~ \\

    public UUID getAddressPK() { return addressPK; }
    public void setAddressPK(UUID val) { this.addressPK = val; }

    public UUID getUserFK() { return userFK; }
    public void setUserFK(UUID val) {  this.userFK = val; }

    @Bindable
    public String getAddressLineOne() { return addressLineOne; }
    public void setAddressLineOne(String val) { this.addressLineOne = val; }

    @Bindable
    public String getAddressLineTwo() { return addressLineTwo; }
    public void setAddressLineTwo(String val) {  this.addressLineTwo = val; }

    @Bindable
    public String getCity() { return city; }
    public void setCity(String val) { this.city = val; }

    @Bindable
    public String getCounty() { return county; }
    public void setCounty(String val) { this.county = val; }

    @Bindable
    public String getPostcode() { return postcode; }
    public void setPostcode(String val) { this.postcode = val; }

    public Calendar getCreatedDate() { return this.createdDate; }
    public void setCreatedDate(Calendar val) { this.createdDate = val; }


    // ~~ Extras ~~ \\

    public User getUser() { return this.user; }
    public void setUser(User model) {
        this.user = model;
        this.setUserFK(model.getUserPK());
    }

    public int saveChanges(Context context) {
        if (Helper.isNullOrEmpty(this.toString())) {
            return -2;  // Because -1 already used for if update method fails
        }

        ILocalStorageProvider<Address> repository = DataStorageFactory.getProvider(context, Address.class);
        return repository.update(this);
    }

    public static boolean isEqual(Address item1, Address item2) {
        if (item1 == null && item2 == null) {
            return true;
        }
        return (item1 != null && item2 != null && item1.getAddressPK().equals(item2.addressPK));
    }

    @Override
    public String toString()
    {
        return _toStringSeparatedBy(", ");
    }

    public String toStringUrl()
    {
        return _toStringSeparatedBy(" ");
    }

    private String _toStringSeparatedBy(String separator)
    {
        String[] addressParts = new String[] { addressLineOne, addressLineTwo, city, county, postcode };
        String ret = "";

        for (int i = 0; i < addressParts.length; i++) {
            ret += (!Helper.isNullOrEmpty(addressParts[i]) ? addressParts[i] + separator : "");
        }
        if (ret.endsWith(", ")) {
            ret = ret.substring(0, ret.length() - 2);
        }

        return ret;
    }

}

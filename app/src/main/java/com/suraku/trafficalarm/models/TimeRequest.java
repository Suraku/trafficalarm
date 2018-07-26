package com.suraku.trafficalarm.models;

import com.suraku.dev.sqlite.SQLiteModelMetadata;

import java.util.Calendar;
import java.util.UUID;

/**
 * Model
 */
public class TimeRequest implements Comparable<TimeRequest>
{
    // Standard empty constructor required by database
    public TimeRequest()
    {
        this.timeRequestPK = UUID.randomUUID();
        this.jsonResponse = "";
        this.durationInTraffic = 0;
        this.createdDate = Calendar.getInstance();
    }

    public TimeRequest(UUID originFK, UUID destinationFK)
    {
        this();
        this.originAddressFK = originFK;
        this.destinationAddressFK = destinationFK;
    }

    @Override
    public int compareTo(TimeRequest item) {
        return item.createdDate.compareTo(this.createdDate);
    }

    @SQLiteModelMetadata
    private UUID timeRequestPK;

    @SQLiteModelMetadata
    private String jsonResponse;

    @SQLiteModelMetadata
    private int durationInTraffic;

    @SQLiteModelMetadata
    private Calendar createdDate;

    @SQLiteModelMetadata
    private UUID originAddressFK;

    @SQLiteModelMetadata
    private UUID destinationAddressFK;


    // ~~ Getters & Setters ~~ \\

    public UUID getTimeRequestPK() { return this.timeRequestPK; }
    public void setTimeRequestPK(UUID val) { this.timeRequestPK = val; }

    public String getJsonResponse() { return this.jsonResponse; }
    public void setJsonResponse(String val) { this.jsonResponse = val; }

    public int getDurationInTraffic() { return this.durationInTraffic; }
    public void setDurationInTraffic(int val) { this.durationInTraffic = val; }

    public Calendar getCreatedDate() { return this.createdDate; }
    public void setCreatedDate(Calendar val) { this.createdDate = val; }

    public UUID getOriginAddressFK() { return this.originAddressFK; }
    public void setOriginAddressFK(UUID val) { originAddressFK = val; }

    public UUID getDestinationAddressFK() { return this.destinationAddressFK; }
    public void setDestinationAddressFK(UUID val) { destinationAddressFK = val; }
}
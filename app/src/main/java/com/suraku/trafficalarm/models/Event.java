package com.suraku.trafficalarm.models;

import android.content.Context;

import com.suraku.dev.sqlite.SQLiteModelMetadata;
import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.data.storage.DataStorageFactory;
import com.suraku.trafficalarm.data.storage.ILocalStorageProvider;

import java.util.Calendar;
import java.util.UUID;

/**
 * Model
 */

public class Event implements Comparable<Event>
{
    public Event()
    {
        this.eventPK = UUID.randomUUID();
        this.createdDate = Calendar.getInstance();
        this.isVisible = this.isReported = 0;
        this.eventLevel = EventLevel.LOW.getNumber();
        this.displayMessage = this.errorMessage = "";
    }

    public Event(User user, boolean isVisible)
    {
        this();
        this.setUserFK(user.getUserPK());
        this.setIsVisible(isVisible);
    }

    @Override
    public int compareTo(Event item) { return item.createdDate.compareTo(this.createdDate); }

    @SQLiteModelMetadata
    private UUID eventPK;

    @SQLiteModelMetadata
    private Calendar createdDate;

    @SQLiteModelMetadata
    private UUID userFK;

    @SQLiteModelMetadata
    private int isVisible;

    @SQLiteModelMetadata
    private int eventLevel;

    @SQLiteModelMetadata
    private int isReported;

    @SQLiteModelMetadata
    private String displayMessage;

    @SQLiteModelMetadata
    private String errorMessage;


    // ~~ Getters & Setters ~~ \\

    public UUID getEventPK() { return eventPK; }
    public void setEventPK(UUID val) { eventPK = val; }

    public Calendar getCreatedDate() { return createdDate; }
    public void setCreatedDate(Calendar val) { createdDate = val; }

    public UUID getUserFK() { return userFK; }
    public void setUserFK(UUID val) { userFK = val; }

    public boolean getIsVisible() { return isVisible != 0; }
    public void setIsVisible(boolean val) { isVisible = val ? 1 : 0; }

    public EventLevel getEventLevel() { return EventLevel.getLevel(eventLevel); }
    public void setEventLevel(EventLevel val) { eventLevel = val.getNumber(); }

    public boolean getIsReported() { return isReported != 0; }
    public void setIsReported(boolean val) { isReported = val ? 1 : 0; }

    public String getDisplayMessage() { return displayMessage; }
    public void setDisplayMessage(String val) { displayMessage = val; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String val) { errorMessage = val; }


    // ~~ Static Methods ~~ \\

    public static int saveEvent(Context context, Event model) {
        @SuppressWarnings("unchecked")
        ILocalStorageProvider<Event> repository = DataStorageFactory.getProvider(context, Event.class);
        return repository.update(model);
    }
}

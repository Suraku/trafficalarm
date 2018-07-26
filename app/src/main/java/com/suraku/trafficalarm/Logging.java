package com.suraku.trafficalarm;

import android.content.Context;
import android.util.Log;

import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.models.Event;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Logging information regarding an exception, with an additional user-friendly message
 */

public final class Logging
{
    public static void logEvent(Context context, String displayMessage, EventLevel eventLevel) {
        // Output to console window
        Log.d("APP", displayMessage);

        Event model = new Event(Helper.getUser(context), true);
        model.setEventLevel(eventLevel);
        model.setDisplayMessage(displayMessage);

        // Save to database
        Event.saveEvent(context, model);
    }

    public static void logEvent(Context context, String displayMessage, EventLevel eventLevel, Exception e) {
        // Output to console window
        Log.d("APP", displayMessage);

        Event model = new Event(Helper.getUser(context), true);
        model.setEventLevel(eventLevel);
        model.setDisplayMessage(displayMessage);
        model.setErrorMessage(_getStacktrace(e));

        // Save to database
        Event.saveEvent(context, model);
    }

    public static void logDebugEvent(Context context, String displayMessage) {
        // Output to console window
        Log.d("APP", displayMessage);

        Event model = new Event(Helper.getUser(context), false);
        model.setEventLevel(EventLevel.DEBUG);
        model.setDisplayMessage(displayMessage);

        // Save to database
        Event.saveEvent(context, model);
    }

    public static void logErrorEvent(Context context, Exception e) {
        // Output to console window
        Log.d("APP", e.toString());
        String errorMsg = _getStacktrace(e);

        Event model = new Event(Helper.getUser(context), false);
        model.setEventLevel(EventLevel.ERROR);
        model.setDisplayMessage(errorMsg);
        model.setErrorMessage(errorMsg);

        // Save to database
        Event.saveEvent(context, model);
    }

    private static String _getStacktrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

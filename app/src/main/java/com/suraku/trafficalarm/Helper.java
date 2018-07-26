package com.suraku.trafficalarm;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.suraku.trafficalarm.data.storage.DataStorageFactory;
import com.suraku.trafficalarm.data.storage.ILocalStorageProvider;
import com.suraku.trafficalarm.models.Alarm;
import com.suraku.trafficalarm.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Helper
{
    public static User getUser(Context context)
    {
        @SuppressWarnings("unchecked")
        ILocalStorageProvider<User> repository = DataStorageFactory.getProvider(context, User.class);
        @SuppressWarnings("unchecked")
        List<User> users = repository.findAll();

        User ret;

        if (users.size() > 0) {
            ret = users.get(0);
        } else {
            ret = new User();
            repository.update(ret);
        }

        return ret;
    }

    public static List<Alarm> getAlarms(Context context) {
        ILocalStorageProvider<Alarm> repository = DataStorageFactory.getProvider(context, Alarm.class);
        List<Alarm> alarms = repository.findAll();

        // Sort and return
        Collections.sort(alarms);
        return alarms;
    }

    public static Alarm getActiveAlarm(Context context)
    {
        ILocalStorageProvider<Alarm> repository = DataStorageFactory.getProvider(context, Alarm.class);
        List<Alarm> alarms = repository.findAll();

        if (alarms.size() <= 0) {
            for (int i = 0; i < 3; i++) {
                Alarm alarm = new Alarm(Helper.getUser(context));
                if (i == 0) alarm.setIsActive(true);

                repository.update(alarm);
                alarms.add(alarm);
            }
        }

        for (int i = 0; i < alarms.size(); i++) {
            if (alarms.get(i).getIsActive()) {
                return alarms.get(i);
            }
        }

        Alarm defaultAlarm = alarms.get(0);
        defaultAlarm.setIsActive(true);
        repository.update(defaultAlarm);

        return defaultAlarm;
    }

    public static void findFiles(String path)
    {
        Log.d("Files", "Path: " + path);

        File f = new File(path);
        File file[] = f.listFiles();

        Log.d("Files", "Size: "+ file.length);

        for (int i=0; i < file.length; i++) {
            Log.d("Files", "FileName:" + file[i].getName());
        }
    }

    public static Map<String, Object> objectPropertiesToList(Object object)
            throws IllegalAccessException
    {
        HashMap<String, Object> ret = new HashMap<>();

        Field[] fields = object.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            Object val = field.get(object);
            ret.put(field.getName(), val);
        }

        return ret;
    }

    public static Boolean isNullOrEmpty(String val)
    {
        return !(val != null && !val.trim().isEmpty());
    }

    public static Method findMethodCaseInsensitive(Class type, String name)
    {
        Method[] methods = type.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().toLowerCase().equals(name.toLowerCase())) {
                return method;
            }
        }
        return null;
    }

    public static Toast createScalableToast(Context context, String message, int duration)
    {
        Toast toast = Toast.makeText(context, message, duration);
        ViewGroup toastView = (ViewGroup) toast.getView();

        int fontDefault = (int) (context.getResources().getDimension(R.dimen.font_size_toast) /
                context.getResources().getDisplayMetrics().density);

        TextView toastText = (TextView) toastView.getChildAt(0);
        toastText.setTextSize(fontDefault);

        return toast;
    }

    public static String getTimeFormatted(Calendar calendar, boolean is24HrFormat)
    {
        Date date = calendar.getTime();

        if (is24HrFormat) {
            DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            return timeFormat.format(date);
        } else {
            DateFormat timeFormat = new SimpleDateFormat("hh:mma", Locale.ENGLISH);
            return timeFormat.format(date).replace("AM", "am").replace("PM", "pm");
        }
    }

    public static String getDateFormatted(Calendar calendar) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy", Locale.ENGLISH);
        Date date = calendar.getTime();
        return dateFormat.format(date);
    }

    public static String getDateTimeFormatted(Calendar calendar, boolean is24HrFormat) {
        return getDateFormatted(calendar) + "  " + getTimeFormatted(calendar, is24HrFormat);
    }

    public static <T> T findJsonObject(Class<T> type, String json, String key) {
        try {
            JSONObject jsonData = new JSONObject(json);
            JSONObject duration = (JSONObject) findJsonObject(jsonData, key);

            return type.cast(findJsonObject(duration, "value"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            return type.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /// Recursive method to continually search JSON tree for key:value pair
    public static Object findJsonObject(JSONObject parent, String key)
    {
        Object ret = null;
        JSONArray keyNames = parent.names();

        for (int i = 0; i < keyNames.length(); i++) {
            try {
                if (ret != null) {
                    return ret;
                }

                String keyName = (String)keyNames.get(i);
                Object item = parent.get(keyName);

                if (keyName.equals(key)) {
                    ret = item;
                    break;
                }

                if (item instanceof JSONObject) {
                    ret = findJsonObject((JSONObject)item, key);
                } else if (item instanceof JSONArray) {
                    JSONArray array = (JSONArray)item;
                    for (int j = 0; j < array.length(); j++) {
                        if (ret != null) {
                            return ret;
                        }
                        if (array.get(j) instanceof JSONObject) {
                            ret = findJsonObject((JSONObject) array.get(j), key);
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }
}

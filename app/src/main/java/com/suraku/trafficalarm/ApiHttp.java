package com.suraku.trafficalarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.models.Address;
import com.suraku.trafficalarm.viewmodels.ApiGMapResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Makes the request to Google Maps traffic API
 */
public class ApiHttp extends AsyncTask<Void, Void, ApiGMapResult>
{
    private GMapResultListener m_gMapResultListener;
    private String mUrl;
    private Context mContext;

    public static final String KEY_DURATION_TRAFFIC = "duration_in_traffic";
    public static final String KEY_DURATION_NORMAL = "duration";

    public interface GMapResultListener {
        void onGMapsResult(ApiGMapResult result);
    }

    public ApiHttp(Context context, String url) {
        m_gMapResultListener = (GMapResultListener) context;
        this.mUrl = url;
        this.mContext = context;
    }

    public ApiHttp(Context context, Address origin, Address destination) {
        this(context, createUrl(context, origin, destination));
    }

    /** ~~~ Executed NOT on UI ~~~ **/
    @Override
    protected ApiGMapResult doInBackground(Void... args)
    {
        return getGMapsAPiRequest(mContext, mUrl);
    }

    /// Executed on UI
    @Override
    protected void onPostExecute(ApiGMapResult result)
    {
        if (m_gMapResultListener != null) {
            m_gMapResultListener.onGMapsResult(result);
        }
    }

    /** Public static methods **/

    public static String createUrl(Context context, Address origin, Address destination)
    {
        String apiKey = null;

        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            apiKey = bundle.getString("com.google.android.geo.API_KEY");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Logging.logEvent(context, context.getString(R.string.logging_general), EventLevel.HIGH, e);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String trafficModel = preferences.getString(
                context.getString(R.string.pref_key_trafficModel),
                context.getString(R.string.pref_defaultValue_trafficModel)
                );

        String mapsBaseUrl = "https://maps.googleapis.com/maps/api/directions/json";
        String mapsBaseParams = "?departure_time=now" + "&traffic_model=" + trafficModel + "&key=" + apiKey;
        String mapsOrigin = "&origin=";
        String mapsDestination = "&destination=";

        try {
            mapsOrigin += URLEncoder.encode(origin.toStringUrl(), "UTF-8");
            mapsDestination += URLEncoder.encode(destination.toStringUrl(), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Logging.logEvent(context, context.getString(R.string.logging_general), EventLevel.HIGH, e);
        }

        // Encoded URL
        return mapsBaseUrl + mapsBaseParams + mapsOrigin + mapsDestination;
    }

    public static ApiGMapResult getGMapsAPiRequest(Context context, String url)
    {
        ApiGMapResult ret = new ApiGMapResult();

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new RuntimeException("Can't perform Maps API request on the UI thread.");
        }

        // Check for connectivity
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        int timeout = 180;
        while (timeout > 0 && !hasInternetConnectivity(cm)) {
            try {
                Thread.sleep(1000);
                timeout--;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();

                String errorMsg = context.getString(R.string.value_logging_apiRequestThread);
                ret.setErrorMessage(errorMsg);
                //Logging.Log(e, errorMsg, EventLevel.HIGH);
                return ret;
            }
        }
        if (!hasInternetConnectivity(cm)) {
            String errorMsg = context.getString(R.string.value_logging_noInternetConnectivity);
            ret.setErrorMessage(errorMsg);
            //Logging.Log(errorMsg, EventLevel.MED);
            return ret;
        }

        // Perform API request
        StringBuilder strBuilder = new StringBuilder("");
        try {
            URL mUrl = new URL(url);

            HttpURLConnection conn = (HttpURLConnection)mUrl.openConnection();
            InputStream inputStream = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                strBuilder.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();

            String errorMsg = context.getString(R.string.value_logging_apiRequestStream);
            //Logging.Log(e, errorMsg, EventLevel.HIGH);
            ret.setErrorMessage(errorMsg);
            return ret;
        }

        Logging.logEvent(context, "API request successful.", EventLevel.MED);

        String strRet = strBuilder.toString();
        ret.setJSONString(strRet);
        ret.setDurationSeconds(Helper.findJsonObject(Integer.class, strRet, KEY_DURATION_TRAFFIC));

        return ret;
    }

    public static String formatTrafficDuration(int durationTraffic, int durationNormal) {
        double minutes = Math.floor((double)durationTraffic / (double)60);
        double secs = durationTraffic % 60;
        int dur = (int)Math.ceil(((double)durationTraffic - (double)durationNormal) / (double)60);

        return (int) minutes + "m " + (int)secs + "s (" + dur + "m)";
    }

    public static boolean hasInternetConnectivity(ConnectivityManager cm) {
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}

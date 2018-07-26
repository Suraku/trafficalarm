package com.suraku.trafficalarm.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.suraku.trafficalarm.ApiHttp;
import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.Notifications;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.viewmodels.MainViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlarmActivity extends BaseActivity implements OnMapReadyCallback
{
    private MediaPlayer mMedia;
    private GoogleMap mMap;
    private String mJsonResult;

    private boolean mLayoutReady = false;
    private CameraUpdate mCameraUpdate = null;


    /** Constructor Arguments **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Logging.logDebugEvent(this, "AlarmActivity_onCreate");

        // Initialize view
        MainViewModel alarm = new MainViewModel(this, Helper.getActiveAlarm(this));
        TextView destinationDisplay = (TextView) findViewById(R.id.activityAlarm_destinationDisplay);
        TextView originDisplay = (TextView) findViewById(R.id.activityAlarm_originDisplay);

        destinationDisplay.setText(alarm.getDisplayDestinationAddress());
        originDisplay.setText(alarm.getDisplayOriginAddress());

        mJsonResult = getIntent().getExtras().getString(getString(R.string.key_apiJsonResult));

        // Set title
        int durationTraffic = Helper.findJsonObject(Integer.class, mJsonResult, ApiHttp.KEY_DURATION_TRAFFIC);
        int durationNormal = Helper.findJsonObject(Integer.class, mJsonResult, ApiHttp.KEY_DURATION_NORMAL);
        setTitle(ApiHttp.formatTrafficDuration(durationTraffic, durationNormal));

        // Prepare map view
        final LinearLayout layout = (LinearLayout) findViewById(R.id.activity_alarm);
        ViewTreeObserver observer = layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mCameraUpdate != null) {
                    mMap.moveCamera(mCameraUpdate);
                }
                mLayoutReady = true;
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.alarmFragment_map);
        mapFragment.getMapAsync(this);

        // Light up lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // Media - default sound
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (uri == null) {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }

        // Media - preference set media file
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean customAlarmEnabled = preferences.getBoolean(getString(R.string.pref_key_customAlarmEnabled), false);
        String alarmFilepath = preferences.getString(getString(R.string.pref_key_alarmFilePath), "");

        File audioFile = new File(alarmFilepath);
        if (audioFile.exists() && customAlarmEnabled) {
            uri = android.net.Uri.parse(audioFile.toURI().toString());
        }

        // Play audio
        mMedia = new MediaPlayer();
        mMedia.setAudioStreamType(AudioManager.STREAM_ALARM);   // Uses alarm volume level
        mMedia.setLooping(true);

        try {
            mMedia.setDataSource(this, uri);
            mMedia.prepare();
            mMedia.start();

        } catch (IOException e) {
            String errorMsg = getString(R.string.logging_alarmActivity_mediaPlayError);
            if (audioFile.exists() && customAlarmEnabled) {
                errorMsg += " " + getString(R.string.logging_alarmActivity_selectNewMedia);
            }

            Logging.logEvent(this, errorMsg, EventLevel.HIGH, e);
        }
    }

    @Override
    public void onStop() {
        _stopMedia();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        _stopMedia();
        super.onDestroy();
    }

    private void _stopMedia() {
        if (mMedia != null && mMedia.isPlaying()) {
            Logging.logDebugEvent(this, "AlarmActivity_stopMedia");

            // Stop media playing
            mMedia.stop();
            mMedia.release();
            mMedia = null;

            // Clear notification if not already cleared
            Notifications.cancelAlarmTrigger(this);
        }
    }

    public void Dismiss_Click(View view) {
        _stopMedia();
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Logging.logDebugEvent(this, "AlarmActivity_onMapReady");
        mMap = googleMap;

        // Display route on map
        drawPath(mJsonResult);
    }

    public void drawPath(String result) {
        try {
            //Transform the string into a json object
            final JSONObject json = new JSONObject(result);

            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");

            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            // Draw onto the map
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)
            );

            // Zoom to location
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            List<LatLng> arr = line.getPoints();
            for (int i = 0; i < arr.size();i++) {
                builder.include(arr.get(i));
            }

            LatLngBounds bounds = builder.build();
            int padding = 80; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            if (mLayoutReady) {
                mMap.moveCamera(cu);
            } else {
                mCameraUpdate = cu;  // Must specify map size otherwise wait for view to be rendered
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}

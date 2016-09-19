package radomski.edu.pl.scalactestapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;


public class PreferenceHelper {
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";


    public static void saveLocation(Context context, String key, LatLng location) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(key + LATITUDE, (float) location.latitude);
        editor.putFloat(key + LONGITUDE, (float) location.longitude);
        editor.apply();
    }


    public static LatLng loadLocation(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        float lat = prefs.getFloat(key + LATITUDE, 0);
        float lng = prefs.getFloat(key + LONGITUDE, 0);
        LatLng latLng = new LatLng(lat, lng);
        return latLng;
    }
}

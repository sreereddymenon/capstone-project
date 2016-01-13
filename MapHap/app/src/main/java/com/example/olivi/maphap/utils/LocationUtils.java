package com.example.olivi.maphap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.olivi.maphap.R;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by olivi on 12/8/2015.
 */
public class LocationUtils {


    private static final String TAG = LocationUtils.class.getSimpleName();

    public static final String PREFS_NAME = "MyPrefsFile";

    //Taken from https://www.geodatasource.com/developers/java

    public static double milesBetweenTwoPoints(double lat1, double lon1, double lat2,
                                               double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        return dist;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }


    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static int getPreferredRadius(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return Integer.parseInt(prefs.getString(context.getString(R.string.pref_radius_key),
                context.getString(R.string.pref_radius_default)));
    }

    public static double getPreferredLatitude(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);

        long lat = prefs.getLong(context.getString(R.string.pref_latitude_key), 0);

        return Double.longBitsToDouble(lat);
    }
    public static double getPreferredLongitude(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);

        long lon = prefs.getLong(context.getString(R.string.pref_longitude_key), 0);

        return Double.longBitsToDouble(lon);
    }

    public static long getPreferredRegionId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getLong(context.getString(R.string.pref_region_id_key), -1);
    }

    public static void saveRegionIdToSharedPref(Context context, long regionId) {
        Log.i(TAG, "saving region ID to shared prefs " + regionId);
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(context.getString(R.string.pref_region_id_key), regionId);
        editor.commit();
    }


    public static void saveLocationToSharedPref(Context context, double latitude, double
            longitude) {
        Log.i(TAG, "saving latitude and longitude to shared prefs " + latitude + " " + longitude);

        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(context.getString(R.string.pref_latitude_key), Double.doubleToLongBits(latitude));
        editor.putLong(context.getString(R.string.pref_longitude_key), Double.doubleToLongBits
                (longitude));
        editor.commit();
    }


}

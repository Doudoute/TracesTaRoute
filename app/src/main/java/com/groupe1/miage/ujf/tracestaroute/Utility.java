package com.groupe1.miage.ujf.tracestaroute;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {
    public static String getPreferredLocation(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatDistance(double dist, boolean isMetric){
        double temp = dist;
        if(!isMetric){
            temp = dist/1.609344;
        }
        return String.format("%.0f", temp);
    }

    public static String formatAltitude(double alt, boolean isMetric){
        double temp = alt;
        if(!isMetric){
            temp = temp/0.3048;
        }
        return String.format("%.0f", temp);
    }
}

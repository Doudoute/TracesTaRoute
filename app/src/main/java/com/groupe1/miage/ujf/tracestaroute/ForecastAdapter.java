package com.groupe1.miage.ujf.tracestaroute;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;

import com.groupe1.miage.ujf.tracestaroute.data.TrackContract;

/**
 * {@link ForecastAdapter} exposes a list of track forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter{
    Context mContext;

    public ForecastAdapter(Context context, Cursor c, int flags){
        super(context, c, flags);
        this.mContext = context;
    }

    /**
     * Transforme les valeurs {longueur, altitude min, latitude max} selon le système de mesure
     * choisis
     */
    private String formatMesure(double distance, double altMin,double altMax){
        boolean isMetric = Utility.isMetric(mContext);
        String mesureStr = Utility.formatDistance(distance, isMetric) + "/" +
                Utility.formatAltitude(altMin, isMetric) + "/" +
                Utility.formatAltitude(altMax, isMetric);
        return mesureStr;
    }

    /*
       This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
       string.
    */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int idx_name = cursor.getColumnIndex(TrackContract.TrackEntry.COLUMN_NAME);
        int idx_sport = cursor.getColumnIndex(TrackContract.TrackEntry.COLUMN_SPORT);
        int idx_short_desc = cursor.getColumnIndex(TrackContract.TrackEntry.COLUMN_SHORT_DESC);
        int idx_length = cursor.getColumnIndex(TrackContract.TrackEntry.COLUMN_LENGTH);
        int idx_alt_min = cursor.getColumnIndex(TrackContract.TrackEntry.COLUMN_MIN_ALTITUDE);
        int idx_alt_max = cursor.getColumnIndex(TrackContract.TrackEntry.COLUMN_MAX_ALTITUDE);

        String formattedMesures = formatMesure(
                cursor.getDouble(ForecastFragment.COL_TRACK_LENGTH),
                cursor.getDouble(ForecastFragment.COL_TRACK_MAX_ALTITUDE),
                cursor.getDouble(ForecastFragment.COL_TRACK_MIN_ALTITUDE));

        return cursor.getString(ForecastFragment.COL_TRACK_NAME) +
                " - " + cursor.getString(ForecastFragment.COL_TRACK_SPORT) +
                " - " + cursor.getString(ForecastFragment.COL_TRACK_SHORT_DESC) +
                " - " + formattedMesures;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        TextView tv = (TextView)view;
        tv.setText(convertCursorRowToUXFormat(cursor));
    }
}

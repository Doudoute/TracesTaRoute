package com.groupe1.miage.ujf.tracestaroute;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of track forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter{

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_FIRST_TRACK = 0;
    private static final int VIEW_TYPE_OTHER_TRACK = 1;

    Context mContext;

    public ForecastAdapter(Context context, Cursor c, int flags){
        super(context, c, flags);
        this.mContext = context;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_FIRST_TRACK: {
                layoutId = R.layout.list_item_forecast_trackone;
                break;
            }
            case VIEW_TYPE_OTHER_TRACK: {
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }
        return LayoutInflater.from(context).inflate(layoutId, parent, false);
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int trackId = cursor.getInt(ForecastFragment.COL_TRACK_ID);
        String sport = cursor.getString(ForecastFragment.COL_TRACK_SPORT);

        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        //mtb = velo
        if(sport.equals("mtb")) {
            iconView.setImageResource(R.drawable.velo);
        }else{
            iconView.setImageResource(R.drawable.rando);
        }

        String name = cursor.getString(ForecastFragment.COL_TRACK_NAME);
        TextView nameView = (TextView) view.findViewById(R.id.list_item_track_textview);
        nameView.setText(name);

        String date = cursor.getString(ForecastFragment.COL_TRACK_CREATION_DATE);
        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        dateView.setText(date);

        boolean isMetric = Utility.isMetric(context);

        String unite = isMetric ? "m" : "ft";

        double altMax = cursor.getDouble(ForecastFragment.COL_TRACK_MAX_ALTITUDE);
        TextView altMaxView = (TextView) view.findViewById(R.id.list_item_altMax_textview);
        altMaxView.setText("Altitude maximum : " + Utility.formatAltitude(altMax,isMetric) + unite);

        double altMin = cursor.getDouble(ForecastFragment.COL_TRACK_MIN_ALTITUDE);
        TextView altMinView = (TextView) view.findViewById(R.id.list_item_altMin_textview);
        altMinView.setText("Altitude minimum : " + Utility.formatAltitude(altMin,isMetric) + unite);

    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_FIRST_TRACK : VIEW_TYPE_OTHER_TRACK;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}

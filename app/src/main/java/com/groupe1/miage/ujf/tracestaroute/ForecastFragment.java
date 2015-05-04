package com.groupe1.miage.ujf.tracestaroute;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.groupe1.miage.ujf.tracestaroute.data.TrackContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int FORECAST_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
            TrackContract.TrackEntry.TABLE_NAME + "." + TrackContract.TrackEntry._ID,
            TrackContract.TrackEntry.COLUMN_TRACK_ID,
            TrackContract.TrackEntry.COLUMN_SPORT,
            TrackContract.TrackEntry.COLUMN_CREATION_DATE,
            TrackContract.TrackEntry.COLUMN_NAME,
            TrackContract.TrackEntry.COLUMN_SHORT_DESC,
            TrackContract.TrackEntry.COLUMN_LENGTH,
            TrackContract.TrackEntry.COLUMN_POSTALCODE,
            TrackContract.TrackEntry.COLUMN_MAX_ALTITUDE,
            TrackContract.TrackEntry.COLUMN_MIN_ALTITUDE,
            TrackContract.TrackEntry.COLUMN_URL,
            TrackContract.LocationEntry.ALIAS_DEPART + "." + TrackContract.LocationEntry.COLUMN_LOC_CITY,
            TrackContract.LocationEntry.ALIAS_DEPART + "." + TrackContract.LocationEntry.COLUMN_LOC_COORD_LAT,
            TrackContract.LocationEntry.ALIAS_DEPART + "." + TrackContract.LocationEntry.COLUMN_LOC_COORD_LONG,
            TrackContract.LocationEntry.ALIAS_ARRIVEE + "." + TrackContract.LocationEntry.COLUMN_LOC_CITY,
            TrackContract.LocationEntry.ALIAS_ARRIVEE + "." + TrackContract.LocationEntry.COLUMN_LOC_COORD_LAT,
            TrackContract.LocationEntry.ALIAS_ARRIVEE + "." + TrackContract.LocationEntry.COLUMN_LOC_COORD_LONG
    };
    static final int COL_TRACK_ID               = 0;
    static final int COL_TRACK_TRACK_ID         = 1;
    static final int COL_TRACK_SPORT            = 2;
    static final int COL_TRACK_CREATION_DATE    = 3;
    static final int COL_TRACK_NAME             = 4;
    static final int COL_TRACK_SHORT_DESC       = 5;
    static final int COL_TRACK_LENGTH           = 6;
    static final int COL_TRACK_POSTALCODE       = 7;
    static final int COL_TRACK_MAX_ALTITUDE     = 8;
    static final int COL_TRACK_MIN_ALTITUDE     = 9;
    static final int COL_TRACK_URL              = 10;
    static final int COL_LOCATION_CITY_D        = 11;
    static final int COL_LOCATION_COORD_LAT_D   = 12;
    static final int COL_LOCATION_COORD_LONG_D  = 13;
    static final int COL_LOCATION_CITY_A        = 14;
    static final int COL_LOCATION_COORD_LAT_A   = 15;
    static final int COL_LOCATION_COORD_LONG_A  = 16;
    private ForecastAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Recup les event du menu refresh
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //recupere le clic sur un menu
        int id = item.getItemId();
        if(id == R.id.action_refresh){
            updateTrack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main,container,false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(TrackContract.TrackEntry.buildTrackUri(cursor.getLong(COL_TRACK_ID)));
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onLocationChanged( ) {
        updateTrack();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void updateTrack() {
        FetchTrackTask trackTask = new FetchTrackTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        trackTask.execute(location);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String postalcode = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = TrackContract.TrackEntry.COLUMN_CREATION_DATE + " ASC";
        Uri trackForLocationUri = TrackContract.TrackEntry.buildTrackLocation(postalcode);

        //NPO : remettre FORECAST_COLUMNS a la place du premier null
        return new CursorLoader(getActivity(),
                trackForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mForecastAdapter.swapCursor(null);
    }
}

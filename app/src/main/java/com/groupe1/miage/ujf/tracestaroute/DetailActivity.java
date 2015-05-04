package com.groupe1.miage.ujf.tracestaroute;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupe1.miage.ujf.tracestaroute.data.TrackContract;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private static final String FORECAST_SHARE_HASHTAG = " #TracesTaRoute";

        private ShareActionProvider mShareActionProvider;
        private String mForecast;

        private static final int DETAIL_LOADER = 0;

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

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // Attach an intent to this ShareActionProvider.  You can update this at any time,
            // like when the user selects a new piece of data they might like to share.
            if (mForecast != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
                    );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoadFinished");
            if (!data.moveToFirst()) {
                return;
            }

            boolean isMetric = Utility.isMetric(getActivity());

            String sport = data.getString(COL_TRACK_SPORT);
            String dateCreation = data.getString(COL_TRACK_CREATION_DATE);
            String name = data.getString(COL_TRACK_NAME);
            String trackDescription = data.getString(COL_TRACK_SHORT_DESC);
            String length = Utility.formatDistance(data.getDouble(COL_TRACK_LENGTH), isMetric);
            String postalcode = data.getString(COL_TRACK_POSTALCODE);
            String altMax = Utility.formatAltitude(data.getDouble(COL_TRACK_MAX_ALTITUDE), isMetric);
            String altMin = Utility.formatAltitude(data.getDouble(COL_TRACK_MIN_ALTITUDE), isMetric);
            String url = data.getString(COL_TRACK_URL);
            String villeDepart = data.getString(COL_LOCATION_CITY_D);
            String latDepart = data.getString(COL_LOCATION_COORD_LAT_D);
            String longDepart = data.getString(COL_LOCATION_COORD_LONG_D);
            String villeArrivee = data.getString(COL_LOCATION_CITY_A);
            String latArrivee = data.getString(COL_LOCATION_COORD_LAT_A);
            String longArrivee = data.getString(COL_LOCATION_COORD_LONG_A);

            mForecast = String.format("Parcours : %s\n" +
                    "Crée le : %s\n" +
                    "Sport : %s\n" +
                    "Description : %s" +
                    "Longueur : %s\n" +
                    "Code postal : %s\n" +
                    "Altitude : {%s - %s} Différence : %f\n" +
                    "Accéder à la trace : %s\n" +
                    "Départ : {%s, latitude : %s, longitude : %s}\n" +
                    "Arrivée : {%s, latitude : %s, longitude : %s}\n",
                    name, dateCreation, sport, trackDescription, length, postalcode, altMin, altMax,
                    (Double.valueOf(altMax) - Double.valueOf(altMin)), url, villeDepart, latDepart,
                    longDepart, villeArrivee, latArrivee, longArrivee);

            TextView detailTextView = (TextView)getView().findViewById(R.id.detail_text);
            detailTextView.setText(mForecast);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) { }
    }
}

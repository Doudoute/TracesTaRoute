package com.groupe1.miage.ujf.tracestaroute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

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

    private void updateTrack() {
        FetchTrackTask trackTask = new FetchTrackTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        trackTask.execute(location);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main,container,false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mForecastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(),DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTrack();
    }

    public class FetchTrackTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG = FetchTrackTask.class.getSimpleName();

        /**
         * Parse le json pour récupérer les informations utiles
         * @param forecastJsonStr
         * @return
         * @throws JSONException
         */
        private String[] getTrackDataFromJson(String forecastJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String LT_RESPONSE = "response";
            final String LT_ID = "id";
            final String LT_SPORT = "sport";
            final String LT_CREATION_DATE = "creation";
            final String LT_NAME = "name";
            final String LT_DESC = "description";
            final String LT_LENGTH = "length";
            final String LT_ALT_MIN = "altMin";
            final String LT_ALT_MAX = "altMax";
            final String LT_URL = "url";
            final String LT_START = "start";
            final String LT_STOP = "stop";
            final String LT_Y = "y";
            final String LT_X = "x";
            final String LT_CITY = "city";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray trackArray = forecastJson.getJSONArray(LT_RESPONSE);

            String[] resultStrs = new String[trackArray.length()];
            for(int i = 0; i < trackArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                int id;
                String sport;
                String dateCreation;
                String name;
                String description;
                double length;
                double altMin;
                double altMax;
                String url;
                double yDepart;
                double xDepart;
                String cityDepart;
                double yArrivee;
                double xArrivee;
                String cityArrivee;

                // Get the JSON object representing the day
                JSONObject numTrackForecast = trackArray.getJSONObject(i);

                id = numTrackForecast.getInt(LT_ID);
                sport = numTrackForecast.getString(LT_SPORT);
                dateCreation = numTrackForecast.getString(LT_CREATION_DATE);
                name = numTrackForecast.getString(LT_NAME);
                description = numTrackForecast.getString(LT_DESC);
                length = numTrackForecast.getDouble(LT_LENGTH);
                altMin = numTrackForecast.getDouble(LT_ALT_MIN);
                altMax = numTrackForecast.getDouble(LT_ALT_MAX);
                url = numTrackForecast.getString(LT_URL);

                JSONObject start = numTrackForecast.getJSONObject(LT_START);
                yDepart = start.getDouble(LT_Y);
                xDepart = start.getDouble(LT_X);
                cityDepart = start.getString(LT_CITY);

                JSONObject stop = numTrackForecast.getJSONObject(LT_STOP);
                yArrivee = stop.getDouble(LT_Y);
                xArrivee = stop.getDouble(LT_X);
                cityArrivee = stop.getString(LT_CITY);

                String tmp = formatMesure(length, altMin, altMax);
                String tabMes[] = tmp.split("/");

                length = Double.valueOf(tabMes[0]);
                altMin = Double.valueOf(tabMes[1]);
                altMax = Double.valueOf(tabMes[2]);

                resultStrs[i] = name + " - " + length + " - " + sport + " - altitude : " + altMin + "-" + altMax;
            }
            return resultStrs;

        }

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            //Pour l'instant, nombre de résultat en dur
            String limit = "14";

            //Clé pour acceder a l'api
            String api_key = "32b8fd610550";

            try {
                final String FORECAST_BASE_URL = "http://api.la-trace.com/v1/track/search/?";
                final String LIMIT_PARAM = "limit";
                final String POSTAL_CODE = "postalcode";
                final String API_KEY = "api_key";
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(POSTAL_CODE, params[0])
                        .appendQueryParameter(API_KEY, api_key)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built Uri " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Forecast JSON String " + forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }
            try{
                return getTrackDataFromJson(forecastJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] results) {
            if(results != null){
                mForecastAdapter.clear();
                for (String trackForecastStr : results){
                    mForecastAdapter.add(trackForecastStr);
                }
                if(results.length == 0){
                    Toast.makeText(getActivity(),R.string.no_track,Toast.LENGTH_SHORT).show();
                }
            }
        }

        private String formatMesure(double distance, double altMin,double altMax){
            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPrefs.getString(
                    getString(R.string.pref_units_key),
                    getString(R.string.pref_units_metric));
            if (unitType.equals(getString(R.string.pref_units_imperial))){
                altMax = (altMax/0.3048);
                altMin = (altMin/0.3048);
                distance = (distance/1.609344);
            }else if(!unitType.equals(getString(R.string.pref_units_metric))){
                Log.d(LOG_TAG,"Type d'unité non trouvé : "+unitType);
            }
            long roundedAltMax = Math.round(altMax);
            long roundedAltMin = Math.round(altMin);
            long roundedDistance = Math.round(distance);

            String mesureStr = roundedDistance+"/"+roundedAltMin+ "/" +roundedAltMax;
            return mesureStr;
        }
    }


}

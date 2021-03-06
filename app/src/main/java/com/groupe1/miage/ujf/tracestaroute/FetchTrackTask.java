/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupe1.miage.ujf.tracestaroute;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.groupe1.miage.ujf.tracestaroute.data.TrackContract;
import com.groupe1.miage.ujf.tracestaroute.data.TrackContract.TrackEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class FetchTrackTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchTrackTask.class.getSimpleName();

    private final Context mContext;

    public FetchTrackTask(Context context) {
        mContext = context;
    }

    private boolean DEBUG = true;

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    long addLocation(String cityName, double lat, double lon) {
        long locationId;

        // On regarde si la location avec nom de ville, x et y existe déja
        Cursor locationCursor = mContext.getContentResolver().query(
                TrackContract.LocationEntry.CONTENT_URI,
                new String[]{TrackContract.LocationEntry._ID},
                TrackContract.LocationEntry.COLUMN_LOC_CITY + " = ? AND " +
                TrackContract.LocationEntry.COLUMN_LOC_COORD_LAT + " = ? AND " +
                TrackContract.LocationEntry.COLUMN_LOC_COORD_LONG + " = ?",
                new String[]{cityName,String.valueOf(lat),String.valueOf(lon)},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(TrackContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            //Création du lieu
            ContentValues locationValues = new ContentValues();

            //Ajout des informations
            locationValues.put(TrackContract.LocationEntry.COLUMN_LOC_CITY, cityName);
            locationValues.put(TrackContract.LocationEntry.COLUMN_LOC_COORD_LAT, lat);
            locationValues.put(TrackContract.LocationEntry.COLUMN_LOC_COORD_LONG, lon);

            //Insertion dans la BD
            Uri insertedUri = mContext.getContentResolver().insert(
                    TrackContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();
        return locationId;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getTrackDataFromJson(String forecastJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
		final String LT_RESPONSE = "response";
		final String LT_ID = "id";
		final String LT_SPORT = "sport";
		final String LT_CREATION_DATE = "creation";
		final String LT_NAME = "name";
		final String LT_DESC = "description";
		final String LT_LENGTH = "length";
		final String LT_POSTALCODE = "postalcode";
		final String LT_ALT_MIN = "altMin";
		final String LT_ALT_MAX = "altMax";
		final String LT_URL = "url";
		final String LT_START = "start";
		final String LT_STOP = "stop";
		final String LT_Y = "y";
		final String LT_X = "x";
		final String LT_CITY = "city";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray trackArray = forecastJson.getJSONArray(LT_RESPONSE);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(trackArray.length());

            for(int i = 0; i < trackArray.length(); i++) {
                // These are the values that will be collected.
                int id;
                String sport;
                String dateCreation;
                String name;
                String description;
                double length;
                int postalcode;
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
                postalcode = numTrackForecast.getInt(LT_POSTALCODE);
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

                //String tmp = formatMesure(length, altMin, altMax);
                //String tabMes[] = tmp.split("/");

                //length = Double.valueOf(tabMes[0]);
                //altMin = Double.valueOf(tabMes[1]);
                //altMax = Double.valueOf(tabMes[2]);

				//Insertion des villes de départ et d'arrivée en BD
				long locationIdD = addLocation(cityDepart, yDepart, xDepart);
				long locationIdA = addLocation(cityArrivee, yArrivee, xArrivee);

                ContentValues trackValues = new ContentValues();

				trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_ID, id);
				trackValues.put(TrackContract.TrackEntry.COLUMN_SPORT, sport);
				trackValues.put(TrackContract.TrackEntry.COLUMN_CREATION_DATE, dateCreation);
				trackValues.put(TrackContract.TrackEntry.COLUMN_NAME, name);
				trackValues.put(TrackContract.TrackEntry.COLUMN_SHORT_DESC, description);
				trackValues.put(TrackContract.TrackEntry.COLUMN_LENGTH, length);
				trackValues.put(TrackContract.TrackEntry.COLUMN_POSTALCODE, postalcode);
				trackValues.put(TrackContract.TrackEntry.COLUMN_MIN_ALTITUDE, altMin);
				trackValues.put(TrackContract.TrackEntry.COLUMN_MAX_ALTITUDE, altMax);
				trackValues.put(TrackContract.TrackEntry.COLUMN_URL, url);
				trackValues.put(TrackContract.TrackEntry.COLUMN_LOC_KEY_DEPART, locationIdD);
				trackValues.put(TrackContract.TrackEntry.COLUMN_LOC_KEY_ARRIVE, locationIdA);

                cVVector.add(trackValues);
            }

            int inserted = 0;

            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(TrackEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchTrackTask Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }
        String postalcodeQuery = params[0];

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
					.appendQueryParameter(POSTAL_CODE, postalcodeQuery)
					.appendQueryParameter(API_KEY, api_key)
					.build();

			URL url = new URL(builtUri.toString());

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
            getTrackDataFromJson(forecastJsonStr);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error ", e);
		} catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }finally{
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
		return null;
    }
}
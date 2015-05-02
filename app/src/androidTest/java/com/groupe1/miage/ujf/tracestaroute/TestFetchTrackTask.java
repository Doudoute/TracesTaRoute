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

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.groupe1.miage.ujf.tracestaroute.data.TrackContract;

public class TestFetchTrackTask extends AndroidTestCase{
    static final String ADD_LOCATION_CITY = "Sunnydale";
    static final double ADD_LOCATION_LAT = 34.425833;
    static final double ADD_LOCATION_LON = -119.714167;

    /*
        Students: uncomment testAddLocation after you have written the AddLocation function.
        This test will only run on API level 11 and higher because of a requirement in the
        content provider.
     */
    @TargetApi(11)
    public void testAddLocation() {
        // start from a clean state
        getContext().getContentResolver().delete(TrackContract.LocationEntry.CONTENT_URI,
                TrackContract.LocationEntry.COLUMN_CITY + " = ?",
                new String[]{ADD_LOCATION_CITY});

        FetchTrackTask fwt = new FetchTrackTask(getContext(), null);
        long locationId = fwt.addLocation(ADD_LOCATION_CITY,ADD_LOCATION_LAT, ADD_LOCATION_LON);

        // does addLocation return a valid record ID?
        assertFalse("Error: addLocation returned an invalid ID on insert", locationId == -1);

        // test all this twice
        for ( int i = 0; i < 2; i++ ) {

            // does the ID point to our location?
            Cursor locationCursor = getContext().getContentResolver().query(
                    TrackContract.LocationEntry.CONTENT_URI,
                    new String[]{
                            TrackContract.LocationEntry._ID,
                            TrackContract.LocationEntry.COLUMN_CITY,
                            TrackContract.LocationEntry.COLUMN_COORD_LAT,
                            TrackContract.LocationEntry.COLUMN_COORD_LONG
                    },
                    TrackContract.LocationEntry.COLUMN_CITY + " = ? AND " +
                    TrackContract.LocationEntry.COLUMN_COORD_LAT + " = ? AND " +
                    TrackContract.LocationEntry.COLUMN_COORD_LONG + " = ?",
                    new String[]{ADD_LOCATION_CITY, String.valueOf(ADD_LOCATION_LAT), String.valueOf(ADD_LOCATION_LON)},
                    null);

            // these match the indices of the projection
            if (locationCursor.moveToFirst()) {
                assertEquals("Error: the queried value of locationId does not match the returned value" +
                        "from addLocation", locationCursor.getLong(0), locationId);
                assertEquals("Error: the queried value of location city is incorrect",
                        locationCursor.getString(1), ADD_LOCATION_CITY);
                assertEquals("Error: the queried value of latitude is incorrect",
                        locationCursor.getDouble(2), ADD_LOCATION_LAT);
                assertEquals("Error: the queried value of longitude is incorrect",
                        locationCursor.getDouble(3), ADD_LOCATION_LON);
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a location query",
                    locationCursor.moveToNext());

            // add the location again
            long newLocationId = fwt.addLocation(ADD_LOCATION_CITY, ADD_LOCATION_LAT,
                    ADD_LOCATION_LON);

            assertEquals("Error: inserting a location again should return the same ID",
                    locationId, newLocationId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(TrackContract.LocationEntry.CONTENT_URI,
                TrackContract.LocationEntry.COLUMN_CITY + " = ?",
                new String[]{ADD_LOCATION_CITY});

        // clean up the test so that other tests can use the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(TrackContract.LocationEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}
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
package com.groupe1.miage.ujf.tracestaroute.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the track database.
 */
public class TrackContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.groupe1.miage.ujf.tracestaroute";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TRACK = "track";
    public static final String PATH_TRACK_LIST = "list";
    public static final String PATH_LOCATION = "location";

    /*
        Inner class that defines the table contents of the location table
        Students: This is where you will add the strings.  (Similar to what has been
        done for TrackEntry)
     */
    public static final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String ALIAS_DEPART = "DEPART";
        public static final String ALIAS_ARRIVEE = "ARRIVEE";

        // Table name
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LOC_CITY = "city";
        public static final String COLUMN_LOC_COORD_LAT = "coord_lat";
        public static final String COLUMN_LOC_COORD_LONG = "coord_long";

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    /* Inner class that defines the table contents of the track table */
    public static final class TrackEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACK).build();

        public static final Uri CONTENT_LIST_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACK).build().buildUpon().appendPath(PATH_TRACK_LIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK + "/" + PATH_TRACK_LIST;

        public static final String TABLE_NAME = "track";

        public static final String COLUMN_TRACK_ID = "track_id";
        public static final String COLUMN_SPORT = "sport";
        public static final String COLUMN_CREATION_DATE = "creation_date";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SHORT_DESC = "short_desc";
        public static final String COLUMN_LENGTH = "length";
        public static final String COLUMN_POSTALCODE = "postalcode";
        public static final String COLUMN_MAX_ALTITUDE = "max";
        public static final String COLUMN_MIN_ALTITUDE = "min";
        public static final String COLUMN_URL = "url";

        // Column with the foreign key into the location table.
        public static final String COLUMN_LOC_KEY_DEPART = "location_id_depart";
        public static final String COLUMN_LOC_KEY_ARRIVE = "location_id_arrive";

        public static Uri buildTrackUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /*
            Student: Fill in this buildTrackLocation function
        */
        public static Uri buildTrackLocation(String locationSetting) {
            return CONTENT_LIST_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildTrackLocationWithCreationDate(String locationSetting, String creationDate){

            return CONTENT_LIST_URI.buildUpon().appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_CREATION_DATE, creationDate).build();
        }

        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(3);
        }

        public static String getIdFromUri(Uri uri){
            return  uri.getPathSegments().get(1);
        }

        public static String getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_CREATION_DATE);
            if (null != dateString && dateString.length() > 0)
                return dateString;
            else
                return "";
        }
    }
}

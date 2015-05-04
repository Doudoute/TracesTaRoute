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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class TrackProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TrackDbHelper mOpenHelper;

    static final int TRACK = 100;
    static final int TRACK_WITH_LOCATION = 101;
    static final int TRACK_WITH_LOCATION_AND_DATE = 102;
    static final int TRACK_WITH_ID = 103;
    static final int LOCATION = 300;

    private static final SQLiteQueryBuilder sTrackByLocationSettingQueryBuilder;

    static{
        sTrackByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        
        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sTrackByLocationSettingQueryBuilder.setTables(
                TrackContract.TrackEntry.TABLE_NAME +
                        " INNER JOIN " + TrackContract.LocationEntry.TABLE_NAME + " AS " +
                        TrackContract.LocationEntry.ALIAS_DEPART + " " + " ON " +
                        TrackContract.TrackEntry.TABLE_NAME + "." +
                        TrackContract.TrackEntry.COLUMN_LOC_KEY_DEPART + " = " +
                        TrackContract.LocationEntry.ALIAS_DEPART + "." +
                        TrackContract.LocationEntry._ID +
                        " INNER JOIN " + TrackContract.LocationEntry.TABLE_NAME + " AS " +
                        TrackContract.LocationEntry.ALIAS_ARRIVEE + " " + " ON " +
                        TrackContract.TrackEntry.TABLE_NAME+ "." +
                        TrackContract.TrackEntry.COLUMN_LOC_KEY_ARRIVE + " = " +
                        TrackContract.LocationEntry.ALIAS_ARRIVEE + "." +
                        TrackContract.LocationEntry._ID
        );
    }

    //location.location_setting = ?
    private static final String sPostalcodeSettingSelection =
            TrackContract.TrackEntry.TABLE_NAME+
                    "." + TrackContract.TrackEntry.COLUMN_POSTALCODE + " = ? ";

    //location.location_setting = ? AND date >= ?
    private static final String sPostalcodeSettingWithStartDateSelection =
            TrackContract.TrackEntry.TABLE_NAME+
                    "." + TrackContract.TrackEntry.COLUMN_POSTALCODE + " = ? AND " +
                    TrackContract.TrackEntry.COLUMN_CREATION_DATE + " >= ? ";

    private static final String sId = TrackContract.TrackEntry.TABLE_NAME + "." +
            TrackContract.TrackEntry._ID + " = ? ";

    private Cursor getTrackByPostalcodeSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = TrackContract.TrackEntry.getLocationSettingFromUri(uri);
        String startDate = TrackContract.TrackEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == "") {
            selection = sPostalcodeSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, startDate};
            selection = sPostalcodeSettingWithStartDateSelection;
        }

        return sTrackByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrackById(Uri uri, String[] projection) {
        String id = TrackContract.TrackEntry.getIdFromUri(uri);

        String[] selectionArgs = new String[]{id};
        String selection = sId;

        return sTrackByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the TRACK, TRACK_WITH_LOCATION, TRACK_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TrackContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, TrackContract.PATH_TRACK + "/#", TRACK_WITH_ID);
        matcher.addURI(authority, TrackContract.PATH_TRACK + "/" + TrackContract.PATH_TRACK_LIST + "/#", TRACK_WITH_LOCATION);
        matcher.addURI(authority, TrackContract.PATH_TRACK + "/" + TrackContract.PATH_TRACK_LIST + "/#/*", TRACK_WITH_LOCATION_AND_DATE);

        matcher.addURI(authority, TrackContract.PATH_TRACK, TRACK);
        matcher.addURI(authority, TrackContract.PATH_LOCATION, LOCATION);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new TrackDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case TRACK_WITH_LOCATION_AND_DATE:
            case TRACK_WITH_LOCATION:
                return TrackContract.TrackEntry.CONTENT_ITEM_TYPE;
            case TRACK:
                return TrackContract.TrackEntry.CONTENT_TYPE;
            case LOCATION:
                return TrackContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case TRACK_WITH_LOCATION_AND_DATE:
            case TRACK_WITH_LOCATION: {
                retCursor = getTrackByPostalcodeSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case TRACK_WITH_ID: {
                retCursor = getTrackById(uri, projection);
                break;
            }
            case TRACK: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TrackContract.TrackEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TrackContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TRACK: {
                long _id = db.insert(TrackContract.TrackEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TrackContract.TrackEntry.buildTrackUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(TrackContract.LocationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TrackContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case TRACK: {
                rowsDeleted = db.delete(TrackContract.TrackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case LOCATION: {
                rowsDeleted = db.delete(TrackContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case TRACK: {
                rowsUpdated = db.update(TrackContract.TrackEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case LOCATION: {
                rowsUpdated = db.update(TrackContract.LocationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRACK:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TrackContract.TrackEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
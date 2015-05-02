package com.groupe1.miage.ujf.tracestaroute.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.groupe1.miage.ujf.tracestaroute.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your TrackContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_LOCATION = "99705";
    static final String TEST_CITY = "North Pole";
    static final String TEST_DATE = "1419033600L";  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Students: Use this to create some default weather values for your database tests.
     */
    static ContentValues createTrackValues(long locationRowIdD, long locationRowIdA) {
        ContentValues trackValues = new ContentValues();
        trackValues.put(TrackContract.TrackEntry.COLUMN_LOC_KEY_DEPART, locationRowIdD);
        trackValues.put(TrackContract.TrackEntry.COLUMN_LOC_KEY_ARRIVE, locationRowIdA);
        trackValues.put(TrackContract.TrackEntry.COLUMN_CREATION_DATE, TEST_DATE);
        trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_ID, 12345);
        trackValues.put(TrackContract.TrackEntry.COLUMN_SHORT_DESC, "Ceci est un test");
        trackValues.put(TrackContract.TrackEntry.COLUMN_MIN_ALTITUDE, 0);
        trackValues.put(TrackContract.TrackEntry.COLUMN_MAX_ALTITUDE, 100);
        trackValues.put(TrackContract.TrackEntry.COLUMN_SPORT, "sport de test");
        trackValues.put(TrackContract.TrackEntry.COLUMN_NAME, "Parcours test");
        trackValues.put(TrackContract.TrackEntry.COLUMN_LENGTH, 10);
        trackValues.put(TrackContract.TrackEntry.COLUMN_URL, "http://test.te");

        return trackValues;
    }

    /*
        Students: You can uncomment this helper function once you have finished creating the
        LocationEntry part of the TrackContract.
     */
    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(TrackContract.LocationEntry.COLUMN_CITY, "North Pole");
        testValues.put(TrackContract.LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(TrackContract.LocationEntry.COLUMN_COORD_LONG, -147.353);

        return testValues;
    }

    /*
        Students: You can uncomment this function once you have finished creating the
        LocationEntry part of the TrackContract as well as the TrackDbHelper.
     */
    static long insertNorthPoleLocationValues(Context context) {
        // insert our test records into the database
        TrackDbHelper dbHelper = new TrackDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        long locationRowId;
        locationRowId = db.insert(TrackContract.LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        return locationRowId;
    }

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}

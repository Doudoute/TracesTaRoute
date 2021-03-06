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

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/*
    Uncomment this class when you are ready to test your UriMatcher.  Note that this class utilizes
    constants that are declared with package protection inside of the UriMatcher, which is why
    the test must be in the same data package as the Android app code.  Doing the test this way is
    a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final String LOCATION_QUERY = "London, UK";
    private static final String TEST_DATE = "102";  // December 20th, 2014
    private static final long TEST_LOCATION_ID = 10L;

    // content://com.example.android.sunshine.app/weather"
    private static final Uri TEST_TRACK_DIR = TrackContract.TrackEntry.CONTENT_URI;
    private static final Uri TEST_TRACK_WITH_LOCATION_DIR = TrackContract.TrackEntry.buildTrackLocation(LOCATION_QUERY);
    private static final Uri TEST_TRACK_WITH_LOCATION_AND_DATE_DIR = TrackContract.TrackEntry.buildTrackLocationWithDate(LOCATION_QUERY, TEST_DATE);
    // content://com.example.android.sunshine.app/location"
    private static final Uri TEST_LOCATION_DIR = TrackContract.LocationEntry.CONTENT_URI;

    /*
        Students: This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = TrackProvider.buildUriMatcher();

        assertEquals("Error: The TRACK URI was matched incorrectly.",
                testMatcher.match(TEST_TRACK_DIR), TrackProvider.TRACK);
        assertEquals("Error: The TRACK WITH LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_TRACK_WITH_LOCATION_DIR), TrackProvider.TRACK_WITH_LOCATION);
        assertEquals("Error: The TRACK WITH LOCATION AND DATE URI was matched incorrectly.",
                testMatcher.match(TEST_TRACK_WITH_LOCATION_AND_DATE_DIR), TrackProvider.TRACK_WITH_LOCATION_AND_DATE);
        assertEquals("Error: The LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_LOCATION_DIR), TrackProvider.LOCATION);
    }
}

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

import android.net.Uri;
import android.test.AndroidTestCase;

/*
    Students: This is NOT a complete test for the TrackContract --- just for the functions
    that we expect you to write.
 */
public class TestTrackContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_TRACK_LOCATION = "/North Pole";
    private static final String TEST_TRACK_DATE = "1419033600L";  // December 20th, 2014

    /*
        Students: Uncomment this out to test your weather location function.
     */
    public void testBuildTrackLocation() {
        Uri locationUri = TrackContract.TrackEntry.buildTrackLocation(TEST_TRACK_LOCATION);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildTrackLocation in " +
                        "TrackContract.",
                locationUri);
        assertEquals("Error: Track location not properly appended to the end of the Uri",
                TEST_TRACK_LOCATION, locationUri.getLastPathSegment());
        assertEquals("Error: Track location Uri doesn't match our expected result",
                locationUri.toString(),
                "content://com.groupe1.miage.ujf.tracestaroute/track/%2FNorth%20Pole");
    }
}
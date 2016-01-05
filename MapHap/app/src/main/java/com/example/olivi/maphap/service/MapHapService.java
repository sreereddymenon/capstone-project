package com.example.olivi.maphap.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.example.olivi.maphap.R;
import com.example.olivi.maphap.data.EventProvider;
import com.example.olivi.maphap.data.EventsAndRegionsColumns;
import com.example.olivi.maphap.data.EventsColumns;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.utils.DateUtils;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


/**
 * Created by olivi on 11/18/2015.
 */

public class MapHapService extends IntentService {
    public static final String LATITUDE_QUERY_EXTRA = "latqe";
    public static final String LONGITUDE_QUERY_EXTRA = "longqe";
    public static final String WITHIN_QUERY_EXTRA = "wqe";

    public static final int VENUES = 0;
    public static final int EVENTS = 1;
    public static final int EVENTS_REGIONS = 2;
    //The order of this array is important for adding data to DB.
    //(ie. Can't add events until you add venues and events_regions until you add events)
    public static final int[] REQUIRED_CONTENT_VALUES = {
            VENUES, EVENTS, EVENTS_REGIONS
    };

    private static final String LOG_TAG = MapHapService.class.getSimpleName();
    public MapHapService() {
        super("MapHap");
    }

    private double mJulianDateAdded;
    private double mLatitude;
    private double mLongitude;
    private int mRadius;

    @Override
    protected void onHandleIntent(Intent intent) {
        mLatitude = intent.getDoubleExtra(LATITUDE_QUERY_EXTRA, 0.00);
        mLongitude = intent.getDoubleExtra(LONGITUDE_QUERY_EXTRA, 0.00);
        mRadius = intent.getIntExtra(WITHIN_QUERY_EXTRA, 50);


        EventsNetworker.HttpRequest request = EventsNetworker.HttpRequest.newBuilder()
                .friendlyName("events_request")
                .latitude(mLatitude)
                .longitude(mLongitude)
                .radius(mRadius)
                .method(EventsNetworker.HttpMethod.GET)
                .authToken(getString(R.string.my_personal_oauth_token))
                .build();
        EventsNetworker.getsInstance(request, getEventsCallbackHandler())
                .execute();

    }

    public EventsNetworker.Callback getEventsCallbackHandler() {
        return new EventsNetworker.Callback() {
            @Override
            public void onResponse(EventsNetworker.HttpResponse result) {
                if (result.statusCode == 200) {
                    try {
                        long regionId = addRegionToDB(mLatitude, mLongitude, mRadius); //TODO this method should throw an exception if it can't write the region to the db

                        EventsDataJsonParser parser =
                                new EventsDataJsonParser(result.body, regionId, mJulianDateAdded);
                        parser.parse();

                        for (int i = 0; i < REQUIRED_CONTENT_VALUES.length; i++) {
                            ContentValues[] cv = parser.getContentValues(REQUIRED_CONTENT_VALUES[i]);
                            addContentValuesToDB(REQUIRED_CONTENT_VALUES[i], cv);
                        }
                        deleteOldData(getApplicationContext());
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "JSON error", e);
                    }
                }
            }

            @Override
            public void onFailure(IOException e) {
                // Show in Stetho :)
            }
        };
    }

    private void addContentValuesToDB(int dataType, ContentValues[] contentValues) {
        Uri contentUri = new Uri.Builder().build();
        switch (dataType) {
            case VENUES:
                contentUri = EventProvider.Venues.CONTENT_URI;
                break;
            case EVENTS:
                contentUri = EventProvider.Events.CONTENT_URI;
                break;
            case EVENTS_REGIONS:
                contentUri = EventProvider.EventsAndRegions.CONTENT_URI;
                break;
        }
        if (contentUri.getPath() == null) {
            throw new IllegalArgumentException("Data type must be one of the three listed in " +
                    "MapHapService.REQUIRED_CONTENT_VALUES");
        } else {
            this.getContentResolver().bulkInsert(contentUri, contentValues);
        }
    }

    public long addRegionToDB(double latitude, double longitude, int within) {
        double dateAdded = DateUtils.getCurrentJulianDateTime();

        Log.i(LOG_TAG, "Date region added: " + dateAdded);

        ContentValues regionCV = new ContentValues();
        regionCV.put(RegionsColumns.LATITUDE, latitude);
        regionCV.put(RegionsColumns.LONGITUDE, longitude);
        regionCV.put(RegionsColumns.RADIUS, within);
        regionCV.put(RegionsColumns.ADDED_DATE_TIME, dateAdded);
        Uri regionUri = this.getContentResolver().insert(EventProvider.Regions.CONTENT_URI,
                regionCV);

        if (regionUri != null) {
            mJulianDateAdded = dateAdded;
        }

        return getIdFromUri(regionUri);
    }

    public static long getIdFromUri(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(1));
    }

    public static int[] deleteOldData(Context context) {
        double cutOffJulian = DateUtils.getCutOffJulianDateTime();

        int[] rowsDeleted = new int[3];

        Log.i(LOG_TAG, "cut off date is " + cutOffJulian);

        rowsDeleted[0] = context.getContentResolver().delete(EventProvider.Regions.CONTENT_URI,
                RegionsColumns.ADDED_DATE_TIME + " <= ?",
                new String[]{Double.toString(cutOffJulian)});

        rowsDeleted[1] = context.getContentResolver().delete(EventProvider.Events.CONTENT_URI,
                EventsColumns.ADDED_DATE_TIME + " <= ?",
                new String[]{Double.toString(cutOffJulian)});

        rowsDeleted[2] = context.getContentResolver().delete(EventProvider
                        .EventsAndRegions.CONTENT_URI,
                EventsAndRegionsColumns.ADDED_DATE_TIME + " <= ?",
                new String[]{Double.toString(cutOffJulian)});

        return rowsDeleted;
    }
}

package com.mynameistodd.autovolume;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.preference.MultiSelectListPreference;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by todd on 2/17/14.
 */
public class CalendarListPreference extends MultiSelectListPreference {

    ContentResolver cr;
    Cursor cursor;
    String[] projection = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.NAME, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
    String selection = "(" + CalendarContract.Calendars.VISIBLE + " = ?)";
    String[] selectionArgs = new String[]{"1"};

    public CalendarListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        List<CharSequence> entries = new ArrayList<CharSequence>();
        List<CharSequence> entriesValues = new ArrayList<CharSequence>();

        cr = context.getContentResolver();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return TODO;

            cursor = cr.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, selectionArgs, null);

            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String displayName = cursor.getString(2);

                entries.add(displayName);
                entriesValues.add(id);
            }

            setEntries(entries.toArray(new CharSequence[]{}));
            setEntryValues(entriesValues.toArray(new CharSequence[]{}));
        } else {
            setEnabled(false);
        }
    }
}

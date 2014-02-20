package com.mynameistodd.autovolume;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.preference.MultiSelectListPreference;
import android.provider.CalendarContract;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by todd on 2/17/14.
 */
public class CalendarListPreference extends MultiSelectListPreference {

    ContentResolver cr;
    Cursor cursor;
    String[] projection = new String[]{CalendarContract.Calendars.NAME, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
    String selection = "(" + CalendarContract.Calendars.VISIBLE + " = ?)";
    String[] selectionArgs = new String[]{"1"};

    public CalendarListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        List<CharSequence> entries = new ArrayList<CharSequence>();
        List<CharSequence> entriesValues = new ArrayList<CharSequence>();

        cr = context.getContentResolver();
        cursor = cr.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, selectionArgs, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String displayName = cursor.getString(1);

            entries.add(name);
            entriesValues.add(displayName);
        }

        setEntries(entries.toArray(new CharSequence[]{}));
        setEntryValues(entriesValues.toArray(new CharSequence[]{}));
    }
}

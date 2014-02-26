package com.mynameistodd.autovolume;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;


/**
 * Created by todd on 2/20/14.
 */
public class CalendarHelper {
    static ContentResolver cr;
    static Cursor cursor;
    static String[] projection = new String[]{CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.TITLE};
    static String selection = "(" + CalendarContract.Instances.CALENDAR_ID + " = ?) AND (" + CalendarContract.Instances.ALL_DAY + " = ?)";
    static String[] selectionArgs;
    static AudioManager audioManager;
    static int currentVolume;
    static List<Integer> recur = new ArrayList<Integer>();

    public static Collection<? extends Alarm> getAllAlarms(Context context) {
        List<Alarm> alarms = new ArrayList<Alarm>();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> calendar_ids = sharedPref.getStringSet(context.getString(R.string.pref_calendar_list_key), null);

        audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        recur.add(-1);

        if (calendar_ids != null) {

            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Calendar nowLocal = Calendar.getInstance();

            for (String cal_id : calendar_ids) {
                selectionArgs = new String[]{cal_id, "0"};
                String julianDay = String.valueOf(Time.getJulianDay(today.getTimeInMillis(), today.getTimeZone().getOffset(today.getTimeInMillis())));

                Uri.Builder builder = CalendarContract.Instances.CONTENT_BY_DAY_URI.buildUpon();
                builder.appendPath(julianDay);
                builder.appendPath(julianDay);

                cr = context.getContentResolver();
                cursor = cr.query(builder.build(), projection, selection, selectionArgs, null);

                while (cursor.moveToNext()) {
                    String id = cursor.getString(0);
                    String eventStart = cursor.getString(1);
                    String eventEnd = cursor.getString(2);
                    String title = cursor.getString(3);

                    //Set starting alarm
                    Calendar calStart = Calendar.getInstance();
                    calStart.setTimeInMillis(Long.parseLong(eventStart));

                    if (calStart.after(nowLocal)) {
                        Alarm alarmStart = new Alarm(context);
                        alarmStart.setHour(calStart.get(Calendar.HOUR_OF_DAY));
                        alarmStart.setMinute(calStart.get(Calendar.MINUTE));
                        alarmStart.setVolume(0);
                        alarmStart.setEnabled(true);
                        alarmStart.setType(Alarm.AlarmType.Calendar);
                        alarmStart.setRecur(recur);
                        alarmStart.setTitle("Event Start: " + title);
                        alarmStart.cancel();
                        alarmStart.schedule();
                        alarms.add(alarmStart);
                    }

                    //Set ending alarm
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTimeInMillis(Long.parseLong(eventEnd));

                    if (calEnd.after(nowLocal)) {
                        Alarm alarmEnd = new Alarm(context);
                        alarmEnd.setHour(calEnd.get(Calendar.HOUR_OF_DAY));
                        alarmEnd.setMinute(calEnd.get(Calendar.MINUTE));
                        alarmEnd.setVolume(currentVolume);
                        alarmEnd.setEnabled(true);
                        alarmEnd.setType(Alarm.AlarmType.Calendar);
                        alarmEnd.setRecur(recur);
                        alarmEnd.setTitle("Event End: " + title);
                        alarmEnd.cancel();
                        alarmEnd.schedule();
                        alarms.add(alarmEnd);
                    }
                }
            }
        }
        return alarms;
    }
}

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
    static String[] projection = new String[]{CalendarContract.Instances._ID, CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.TITLE};
    static String selection = "(" + CalendarContract.Instances.CALENDAR_ID + " = ?) AND (" + CalendarContract.Instances.ALL_DAY + " = ?)";
    static String[] selectionArgs;
    static AudioManager audioManager;
    static int restoreVolume;
    static List<Integer> recur = new ArrayList<Integer>();

    public static Collection<? extends Alarm> getAllAlarms(Context context) {
        List<Alarm> alarms = new ArrayList<Alarm>();

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean enabled = sharedPref.getBoolean(context.getString(R.string.pref_calendar_enabled_key), Boolean.parseBoolean(context.getString(R.string.pref_calendar_enabled_default)));
        Set<String> calendar_ids = sharedPref.getStringSet(context.getString(R.string.pref_calendar_list_key), null);
        restoreVolume = sharedPref.getInt(context.getString(R.string.pref_calendar_volumeLevel_restore_key), audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));

        recur.add(-1);

        if (enabled && calendar_ids != null) {

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
                    String instanceID = cursor.getString(0);
                    String eventID = cursor.getString(1);
                    String eventStart = cursor.getString(2);
                    String eventEnd = cursor.getString(3);
                    String title = cursor.getString(4);

                    //Set starting alarm
                    Calendar calStart = Calendar.getInstance();
                    calStart.setTimeInMillis(Long.parseLong(eventStart));

                    if (calStart.after(nowLocal)) {
                        Alarm alarmStart = MySQLiteOpenHelper.getAlarm(context, instanceID + "S", true);
                        if (alarmStart == null) {
                            alarmStart = new Alarm(context);
                        }
                        alarmStart.cancel();
                        alarmStart.setHour(calStart.get(Calendar.HOUR_OF_DAY));
                        alarmStart.setMinute(calStart.get(Calendar.MINUTE));
                        alarmStart.setVolume(0);
                        alarmStart.setEnabled(true);
                        alarmStart.setType(Alarm.AlarmType.Calendar);
                        alarmStart.setRecur(recur);
                        alarmStart.setTitle("Event Start: " + title);
                        alarmStart.setInstanceID(instanceID + "S");
                        alarmStart.schedule();
                        alarmStart.save();
                        alarms.add(alarmStart);
                    }

                    //Set ending alarm
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTimeInMillis(Long.parseLong(eventEnd));

                    if (calEnd.after(nowLocal)) {
                        Alarm alarmEnd = MySQLiteOpenHelper.getAlarm(context, instanceID + "E", true);
                        if (alarmEnd == null) {
                            alarmEnd = new Alarm(context);
                        }
                        alarmEnd.cancel();
                        alarmEnd.setHour(calEnd.get(Calendar.HOUR_OF_DAY));
                        alarmEnd.setMinute(calEnd.get(Calendar.MINUTE));
                        alarmEnd.setVolume(restoreVolume);
                        alarmEnd.setEnabled(true);
                        alarmEnd.setType(Alarm.AlarmType.Calendar);
                        alarmEnd.setRecur(recur);
                        alarmEnd.setTitle("Event End: " + title);
                        alarmEnd.setInstanceID(instanceID + "E");
                        alarmEnd.schedule();
                        alarmEnd.save();
                        alarms.add(alarmEnd);
                    }
                }
            }
        }
        return alarms;
    }
}

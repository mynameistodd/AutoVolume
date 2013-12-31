package com.mynameistodd.autovolume;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by todd on 12/28/13.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "auto_volume.db";
    private static final int DATABASE_VERSION = 1;
    public static final String ALARM_TABLE_NAME = "alarm";
    public static final String ALARM_COLUMN_HOUR = "hour";
    public static final String ALARM_COLUMN_MINUTE = "minute";
    public static final String ALARM_COLUMN_RECUR = "recur";
    public static final String ALARM_COLUMN_ENABLED = "enabled";
    public static final String ALARM_COLUMN_VOLUME = "volume";
    private static final String ALARM_TABLE_CREATE =
            "CREATE TABLE " + ALARM_TABLE_NAME + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                    ALARM_COLUMN_HOUR + " INTEGER, " +
                    ALARM_COLUMN_MINUTE + " INTEGER, " +
                    ALARM_COLUMN_RECUR + " TEXT, " +
                    ALARM_COLUMN_ENABLED + " INTEGER, " +
                    ALARM_COLUMN_VOLUME + " INTEGER);";

    MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ALARM_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2)
        {
        }
    }

    public static boolean insertAlarm(Context context, Alarm alarm)
    {
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues ct = new ContentValues();
        ct.put(ALARM_COLUMN_HOUR, alarm.getHour());
        ct.put(ALARM_COLUMN_MINUTE, alarm.getMinute());
        ct.put(ALARM_COLUMN_RECUR, Util.getRecurDelim(alarm.getRecur(), "|"));
        ct.put(ALARM_COLUMN_ENABLED, alarm.isEnabled());
        ct.put(ALARM_COLUMN_VOLUME, alarm.getVolume());
        long rows = db.insert(ALARM_TABLE_NAME, null, ct);

        if (rows > 0) { return true; } else { return false; }
    }

    public static boolean deleteAlarm(Context context, Alarm alarm)
    {
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        String whereClause = BaseColumns._ID + " = ?";
        String[] whereArgs = new String[] {String.valueOf(alarm.getId())};
        int rows = db.delete(ALARM_TABLE_NAME, whereClause, whereArgs);

        if (rows > 0) { return true; } else { return false; }
    }

    public static List<Alarm> getAllAlarms(Context context) {
        List<Alarm> alarms = new ArrayList<Alarm>();
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor dbCursor = db.query(ALARM_TABLE_NAME, null, null,null,null,null,null);
        while (dbCursor.moveToNext())
        {
            Alarm alarm = new Alarm(context);
            alarm.setId(dbCursor.getInt(dbCursor.getColumnIndex(BaseColumns._ID)));
            alarm.setHour(dbCursor.getInt(dbCursor.getColumnIndex(ALARM_COLUMN_HOUR)));
            alarm.setMinute(dbCursor.getInt(dbCursor.getColumnIndex(ALARM_COLUMN_MINUTE)));
            alarm.setRecur(Util.getRecurList(dbCursor.getString(dbCursor.getColumnIndex(ALARM_COLUMN_RECUR))));
            alarm.setEnabled(dbCursor.getInt(dbCursor.getColumnIndex(ALARM_COLUMN_ENABLED)) > 0);
            alarm.setVolume(dbCursor.getInt(dbCursor.getColumnIndex(ALARM_COLUMN_VOLUME)));
            alarms.add(alarm);
        }
        return alarms;
    }
}

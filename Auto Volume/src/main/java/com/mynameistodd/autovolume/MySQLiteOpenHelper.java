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

    public static final String ALARM_TABLE_NAME = "alarm";
    public static final String ALARM_COLUMN_HOUR = "hour";
    public static final String ALARM_COLUMN_MINUTE = "minute";
    public static final String ALARM_COLUMN_RECUR = "recur";
    public static final String ALARM_COLUMN_ENABLED = "enabled";
    public static final String ALARM_COLUMN_VOLUME = "volume";
    public static final String ALARM_COLUMN_TITLE = "title";
    public static final String ALARM_COLUMN_INSTANCE_ID = "instanceID";
    private static final String DATABASE_NAME = "auto_volume.db";
    private static final int DATABASE_VERSION = 2;
    private static final String ALARM_TABLE_CREATE =
            "CREATE TABLE " + ALARM_TABLE_NAME + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                    ALARM_COLUMN_HOUR + " INTEGER, " +
                    ALARM_COLUMN_MINUTE + " INTEGER, " +
                    ALARM_COLUMN_RECUR + " TEXT, " +
                    ALARM_COLUMN_ENABLED + " INTEGER, " +
                    ALARM_COLUMN_VOLUME + " INTEGER, " +
                    ALARM_COLUMN_TITLE + " TEXT, " +
                    ALARM_COLUMN_INSTANCE_ID + " TEXT);";

    MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static int insertAlarm(Context context, Alarm alarm)
    {
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues ct = new ContentValues();
        ct.put(ALARM_COLUMN_HOUR, alarm.getHour());
        ct.put(ALARM_COLUMN_MINUTE, alarm.getMinute());
        ct.put(ALARM_COLUMN_RECUR, Util.getRecurDelim(alarm.getRecur(), "|"));
        ct.put(ALARM_COLUMN_ENABLED, alarm.isEnabled());
        ct.put(ALARM_COLUMN_VOLUME, alarm.getVolume());
        ct.put(ALARM_COLUMN_TITLE, alarm.getTitle());
        ct.put(ALARM_COLUMN_INSTANCE_ID, alarm.getInstanceID());
        long rows = db.insert(ALARM_TABLE_NAME, null, ct);

        db.close();
        helper.close();
        return (int) rows;
    }

    public static boolean deleteAlarm(Context context, Alarm alarm)
    {
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        String whereClause = BaseColumns._ID + " = ?";
        String[] whereArgs = new String[] {String.valueOf(alarm.getId())};
        int rows = db.delete(ALARM_TABLE_NAME, whereClause, whereArgs);

        db.close();
        helper.close();
        return rows > 0;
    }

    public static Alarm getAlarm(Context context, String id, boolean isInstanceID)
    {
        Alarm alarm = null;
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        String whereClause = (!isInstanceID) ? BaseColumns._ID + " = ?" : ALARM_COLUMN_INSTANCE_ID + " = ?";
        String[] whereArgs = new String[]{id};
        Cursor dbCursor = db.query(ALARM_TABLE_NAME, null, whereClause, whereArgs, null, null, null);
        if (dbCursor.moveToFirst())
        {
            alarm = new Alarm(context);
            alarm.setId(dbCursor.getInt(dbCursor.getColumnIndex(BaseColumns._ID)));
            alarm.setHour(dbCursor.getInt(dbCursor.getColumnIndex(ALARM_COLUMN_HOUR)));
            alarm.setMinute(dbCursor.getInt(dbCursor.getColumnIndex(ALARM_COLUMN_MINUTE)));
            alarm.setRecur(Util.getRecurList(dbCursor.getString(dbCursor.getColumnIndex(ALARM_COLUMN_RECUR))));
            alarm.setEnabled(dbCursor.getInt(dbCursor.getColumnIndex(ALARM_COLUMN_ENABLED)) > 0);
            alarm.setVolume(dbCursor.getInt(dbCursor.getColumnIndex(ALARM_COLUMN_VOLUME)));
            alarm.setTitle(dbCursor.getString(dbCursor.getColumnIndex(ALARM_COLUMN_TITLE)));
            alarm.setInstanceID(dbCursor.getString(dbCursor.getColumnIndex(ALARM_COLUMN_INSTANCE_ID)));
            if (alarm.getInstanceID() != null) {
                alarm.setType(Alarm.AlarmType.Calendar);
            } else {
                alarm.setType(Alarm.AlarmType.Timed);
            }
        }
        db.close();
        helper.close();
        return alarm;
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
            alarm.setTitle(dbCursor.getString(dbCursor.getColumnIndex(ALARM_COLUMN_TITLE)));
            alarm.setInstanceID(dbCursor.getString(dbCursor.getColumnIndex(ALARM_COLUMN_INSTANCE_ID)));
            if (alarm.getInstanceID() == null) {
                alarm.setType(Alarm.AlarmType.Timed);
                alarms.add(alarm);
            }
        }
        db.close();
        helper.close();
        return alarms;
    }

    public static boolean updateAlarm(Context context, Alarm alarm) {
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues ct = new ContentValues();
        String whereClause = BaseColumns._ID + " = ?";
        String[] whereArgs = new String[] {String.valueOf(alarm.getId())};
        ct.put(BaseColumns._ID, alarm.getId());
        ct.put(ALARM_COLUMN_HOUR, alarm.getHour());
        ct.put(ALARM_COLUMN_MINUTE, alarm.getMinute());
        ct.put(ALARM_COLUMN_RECUR, Util.getRecurDelim(alarm.getRecur(), "|"));
        ct.put(ALARM_COLUMN_ENABLED, alarm.isEnabled());
        ct.put(ALARM_COLUMN_VOLUME, alarm.getVolume());
        ct.put(ALARM_COLUMN_TITLE, alarm.getTitle());
        ct.put(ALARM_COLUMN_INSTANCE_ID, alarm.getInstanceID());
        int rows = db.update(ALARM_TABLE_NAME, ct, whereClause, whereArgs);

        db.close();
        helper.close();
        return rows > 0;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ALARM_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2)
        {
            String alterAddTitle = "ALTER TABLE " + ALARM_TABLE_NAME + " ADD COLUMN " +
                    ALARM_COLUMN_TITLE + " TEXT";
            String alterAddInstanceID = "ALTER TABLE " + ALARM_TABLE_NAME + " ADD COLUMN " +
                    ALARM_COLUMN_INSTANCE_ID + " TEXT";
            db.execSQL(alterAddTitle);
            db.execSQL(alterAddInstanceID);
        }
    }
}

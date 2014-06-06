package com.mynameistodd.autovolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import java.util.ArrayList;
import java.util.List;

public class BootTimeZoneBR extends BroadcastReceiver {

    private List<Alarm> allAlarms;

    public BootTimeZoneBR() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Util.MYNAMEISTODD, "(Re)setting alarms...");

        Tracker easyTracker = EasyTracker.getInstance(context);
        easyTracker.send(MapBuilder.createEvent("setting_alarms", (intent.getAction() == Intent.ACTION_TIMEZONE_CHANGED) ? "timezone_changed" : "boot_completed", null, null).build());

        allAlarms = new ArrayList<Alarm>();
        allAlarms.addAll(MySQLiteOpenHelper.getAllAlarms(context));
        allAlarms.addAll(CalendarHelper.getAllAlarms(context));

        for (Alarm alarm : allAlarms) {
            if (intent.getAction() == Intent.ACTION_TIMEZONE_CHANGED) {
                //Cancel the alarms already scheduled if changing time zones.
                alarm.cancel();
            }
            alarm.schedule();
        }
    }

}

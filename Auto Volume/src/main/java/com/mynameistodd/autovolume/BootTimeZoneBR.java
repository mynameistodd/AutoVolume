package com.mynameistodd.autovolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

public class BootTimeZoneBR extends BroadcastReceiver {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    private List<Alarm> allAlarms;

    public BootTimeZoneBR() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Util.MYNAMEISTODD, "(Re)setting alarms...");

        analytics = GoogleAnalytics.getInstance(context);
        tracker = analytics.newTracker(R.xml.global_tracker);

        tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("setting_alarms")
                        .setAction((intent.getAction() == Intent.ACTION_TIMEZONE_CHANGED) ? "timezone_changed" : "boot_completed")
                        .build()
        );

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

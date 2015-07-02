package com.mynameistodd.autovolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.List;

public class CalendarChangedReceiver extends BroadcastReceiver {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    private List<Alarm> alarms;

    public CalendarChangedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        analytics = GoogleAnalytics.getInstance(context);
        tracker = analytics.newTracker(R.xml.global_tracker);

        tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("setting_alarms")
                        .setAction((intent.getAction() == Intent.ACTION_TIMEZONE_CHANGED) ? "calendar_changed" : "unknown")
                        .build()
        );

        alarms = (List<Alarm>) CalendarHelper.getAllAlarms(context);
    }
}

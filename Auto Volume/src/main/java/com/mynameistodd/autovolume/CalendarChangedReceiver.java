package com.mynameistodd.autovolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import java.util.List;

public class CalendarChangedReceiver extends BroadcastReceiver {

    private List<Alarm> alarms;

    public CalendarChangedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Tracker easyTracker = EasyTracker.getInstance(context);
        easyTracker.send(MapBuilder.createEvent("setting_alarms", (intent.getAction() == Intent.ACTION_PROVIDER_CHANGED) ? "calendar_changed" : "unknown", null, null).build());

        alarms = (List<Alarm>) CalendarHelper.getAllAlarms(context);
    }
}

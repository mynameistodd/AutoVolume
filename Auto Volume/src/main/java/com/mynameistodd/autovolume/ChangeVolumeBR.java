package com.mynameistodd.autovolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;

public class ChangeVolumeBR extends BroadcastReceiver {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    public void onReceive(Context context, Intent intent) {

        analytics = GoogleAnalytics.getInstance(context);
        tracker = analytics.newTracker(R.xml.global_tracker);

        tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("volume_change")
                        .setAction("power_connected")
                        .build()
        );

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isEnabled = sharedPrefs.getBoolean(context.getString(R.string.pref_enabled_key), true);
        int volumeLevel = sharedPrefs.getInt(context.getString(R.string.pref_volumeLevel_key), 0);
        String startTime = sharedPrefs.getString(context.getString(R.string.pref_startTime_key), "10:00");
        String endTime = sharedPrefs.getString(context.getString(R.string.pref_endTime_key), "7:00");

        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int startMin = Integer.parseInt(startTime.split(":")[1]);
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, startMin);

        int endHour = Integer.parseInt(endTime.split(":")[0]);
        int endMin = Integer.parseInt(endTime.split(":")[1]);
        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, endHour);
        end.set(Calendar.MINUTE, endMin);
        if (end.before(start)) {
            end.add(Calendar.DAY_OF_MONTH, 1);
        }

        Calendar now = Calendar.getInstance();

        if (isEnabled && now.after(start) && now.before(end)) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, volumeLevel, AudioManager.FLAG_SHOW_UI);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volumeLevel, AudioManager.FLAG_SHOW_UI);
        }

    }

}

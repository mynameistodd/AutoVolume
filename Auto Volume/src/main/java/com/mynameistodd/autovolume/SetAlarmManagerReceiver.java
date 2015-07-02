package com.mynameistodd.autovolume;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.NumberFormat;

public class SetAlarmManagerReceiver extends BroadcastReceiver {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    private AudioManager audioManager;
	private Integer audioLevel;
    private Boolean enabled;
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		audioManager = (AudioManager) arg0.getSystemService(Context.AUDIO_SERVICE);
		audioLevel = arg1.getIntExtra("AUDIO_LEVEL", 0);
        enabled = arg1.getBooleanExtra("ENABLED", false);

        analytics = GoogleAnalytics.getInstance(arg0);
        tracker = analytics.newTracker(R.xml.global_tracker);

        tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("volume_change")
                        .setAction((enabled) ? "alarm_enabled" : "alarm_disabled")
                        .setLabel("level")
                        .setValue(audioLevel.longValue())
                        .build()
        );

        if (enabled) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(arg0);
            boolean pref_notify = sharedPref.getBoolean(arg0.getString(R.string.pref_notify_key), false);

            audioManager.setStreamVolume(AudioManager.STREAM_RING, audioLevel, (pref_notify) ? AudioManager.FLAG_SHOW_UI : 0);

            Log.d(Util.MYNAMEISTODD, "Data from Intent:" + Uri.decode(arg1.getData().toString()));
            Log.d(Util.MYNAMEISTODD, "Volume set to:" + audioLevel);

            if (pref_notify) {
                Integer maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                double toFormat = (audioLevel.floatValue() / maxVolume.floatValue());

                Toast.makeText(arg0, "Volume changed!", Toast.LENGTH_SHORT).show();
                Intent notificationIntent = new Intent(arg0, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(arg0, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager mNotificationManager = (NotificationManager) arg0.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification.Builder(arg0)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Volume changed")
                        .setContentText("Scheduled volume change")
                        .setContentInfo("Set to: " + NumberFormat.getPercentInstance().format(toFormat))
                        .setContentIntent(contentIntent)
                        .build();
                mNotificationManager.notify(1, notification);
            }
        }
	}

}

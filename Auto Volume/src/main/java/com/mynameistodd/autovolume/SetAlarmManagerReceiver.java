package com.mynameistodd.autovolume;

import java.text.NumberFormat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

public class SetAlarmManagerReceiver extends BroadcastReceiver {

	private AudioManager audioManager;
	private Integer audioLevel;
	private Integer maxVolume;
    private Boolean enabled;
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		audioManager = (AudioManager) arg0.getSystemService(Context.AUDIO_SERVICE);
		audioLevel = arg1.getIntExtra("AUDIO_LEVEL", 0);
        enabled = arg1.getBooleanExtra("ENABLED", false);

        if (enabled) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(arg0);
            boolean pref_notify = sharedPref.getBoolean("pref_notify", false);

            audioManager.setStreamVolume(AudioManager.STREAM_RING, audioLevel, AudioManager.FLAG_SHOW_UI);

            Toast.makeText(arg0, "Volume changed!", Toast.LENGTH_SHORT).show();

            Log.d(Util.MYNAMEISTODD, "Data from Intent:" + Uri.decode(arg1.getData().toString()));
            Log.d(Util.MYNAMEISTODD, "Volume set to:" + audioLevel);

            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            double toFormat = (audioLevel.floatValue() / maxVolume.floatValue());

            if (pref_notify) {
                NotificationManager mNotificationManager = (NotificationManager) arg0.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification(R.drawable.ic_launcher, "Volume has been changed", System.currentTimeMillis());
                notification.flags = Notification.FLAG_AUTO_CANCEL;

                Intent notificationIntent = new Intent(arg0, ListAlarms.class);
                PendingIntent contentIntent = PendingIntent.getActivity(arg0, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                notification.setLatestEventInfo(arg0, "Scheduled volume change", "Set to: " + NumberFormat.getPercentInstance().format(toFormat), contentIntent);
                mNotificationManager.notify(1, notification);
            }
        }
//		NotificationCompat.Builder mBuilder =
//		        new NotificationCompat.Builder(arg0)
//		        .setSmallIcon(R.drawable.ic_launcher)
//		        .setContentTitle("My notification")
//		        .setContentText("Hello World!");
//		// Creates an explicit intent for an Activity in your app
//		Intent resultIntent = new Intent(arg0, ListAlarms.class);
//
//		// The stack builder object will contain an artificial back stack for the
//		// started Activity.
//		// This ensures that navigating backward from the Activity leads out of
//		// your application to the Home screen.
//		TaskStackBuilder stackBuilder = TaskStackBuilder.create(arg0);
//		// Adds the back stack for the Intent (but not the Intent itself)
//		stackBuilder.addParentStack(ListAlarms.class);
//		// Adds the Intent that starts the Activity to the top of the stack
//		stackBuilder.addNextIntent(resultIntent);
//		PendingIntent resultPendingIntent =
//		        stackBuilder.getPendingIntent(
//		            0,
//		            PendingIntent.FLAG_UPDATE_CURRENT
//		        );
//		mBuilder.setContentIntent(resultPendingIntent);
//		NotificationManager mNotificationManager =
//		    (NotificationManager) arg0.getSystemService(Context.NOTIFICATION_SERVICE);
//		// mId allows you to update the notification later on.
//		mNotificationManager.notify(1, mBuilder.build());
	}

}

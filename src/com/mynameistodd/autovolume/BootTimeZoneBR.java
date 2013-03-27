package com.mynameistodd.autovolume;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

public class BootTimeZoneBR extends BroadcastReceiver {
	
	private AlarmManager alarmManager;
	private SharedPreferences prefs;
	//private Editor prefsEditor;
	private static List<Integer> recurDays;
	//private List<Alarm> allAlarms;
	
    public BootTimeZoneBR() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d(Util.MYNAMEISTODD, "Setting alarms...");
    	
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	prefs = context.getSharedPreferences(Util.AUTOVOLUME, Context.MODE_PRIVATE);
		//prefsEditor = prefs.edit();
    	
		//allAlarms = new ArrayList<Alarm>();

		int tmpHour;
		int tmpMinute;
		List<Integer> tmpRecur;
		boolean tmpEnabled;
		int tmpVolume;
		
		Map<String, ?> allPrefs = prefs.getAll();
		for (String key : allPrefs.keySet()) {
			if (!key.startsWith("pref")) {
				String[] timeRecur = prefs.getString(key, "").split(":");
				
				tmpHour = Integer.parseInt(timeRecur[0]);
				tmpMinute = Integer.parseInt(timeRecur[1]);
				tmpRecur = Util.getRecurList(timeRecur[2]);
				tmpEnabled = Boolean.parseBoolean( (timeRecur.length > 3) ? timeRecur[3] : "true");
				tmpVolume = Integer.parseInt( (timeRecur.length > 4) ? timeRecur[4] : "0");
				
				//Alarm newAlarm = new Alarm(Long.parseLong(key), tmpHour, tmpMinute, tmpRecur, tmpVolume, tmpEnabled, context);
				//allAlarms.add(newAlarm);
			
		
				//String[] recurDaysArray = tmp.get("RECUR").split("\\|");
				
	//			for (String rd : recurDaysArray) {
	//				if (rd.length() > 0) {
	//					int rdi = Integer.parseInt(rd);
	//					recurDays.add(rdi);
	//					Log.d(Util.MYNAMEISTODD, "recurDays add: " + rdi);
	//				}
	//			}
				
				final Calendar calNow = Calendar.getInstance();
	//			int hour = Integer.parseInt(timeRecur[0]);
	//			int minute = Integer.parseInt(timeRecur[1]);
	//			boolean enabled = Boolean.parseBoolean(tmp.get("ENABLED"));
	//			int nPickerVal = Integer.parseInt((String) allPrefs.get(key));
				
				for (int recurDay : tmpRecur) {
					
					if (intent.getAction() == Intent.ACTION_TIMEZONE_CHANGED) {
						//Cancel the alarms already scheduled
						PendingIntent pendingIntent = Util.createPendingIntent(context, tmpHour, tmpMinute, tmpVolume, recurDay, tmpEnabled);
						alarmManager.cancel(pendingIntent);
						
						//prefsEditor.remove(hour + ":" + minute + ":" + tmp.get("RECUR"));
						//Log.d(Util.MYNAMEISTODD, "Deleted: " + hour + ":" + minute + ":" + tmp.get("RECUR") + " Volume: " + nPickerVal);
					}
					
					if (recurDay != -1) {
						Calendar cNew = Calendar.getInstance();
						cNew.set(Calendar.DAY_OF_WEEK, recurDay+1);
						cNew.set(Calendar.HOUR_OF_DAY, tmpHour);
						cNew.set(Calendar.MINUTE, tmpMinute);
						cNew.set(Calendar.SECOND, 0);
						if (cNew.before(calNow)) {
							cNew.roll(Calendar.WEEK_OF_YEAR, 1);
						}
						if (tmpEnabled) {
							//Set alarms
							PendingIntent pendingIntent = Util.createPendingIntent(context, tmpHour, tmpMinute, tmpVolume, recurDay, tmpEnabled);
							alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cNew.getTimeInMillis(), 604800000, pendingIntent);
							
							Log.d(Util.MYNAMEISTODD, "Repeating: " + DateUtils.formatDateTime(context, cNew.getTimeInMillis(), (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
						}
					}
					else
					{
						Calendar cNew = Calendar.getInstance();
						cNew.set(Calendar.HOUR_OF_DAY, tmpHour);
						cNew.set(Calendar.MINUTE, tmpMinute);
						cNew.set(Calendar.SECOND, 0);
						if (tmpEnabled && cNew.after(calNow)) {
							
							//Set one-time alarm
							PendingIntent pendingIntent = Util.createPendingIntent(context, tmpHour, tmpMinute, tmpVolume, cNew.get(Calendar.DAY_OF_WEEK), tmpEnabled);
							alarmManager.set(AlarmManager.RTC_WAKEUP, cNew.getTimeInMillis(), pendingIntent);
							
							Log.d(Util.MYNAMEISTODD, "One-Time: " + DateUtils.formatDateTime(context, cNew.getTimeInMillis(), (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
						}
					}
				}
			}
		}
    }
	
}

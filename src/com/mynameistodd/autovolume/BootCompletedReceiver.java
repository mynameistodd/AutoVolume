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

public class BootCompletedReceiver extends BroadcastReceiver {
	
	private AlarmManager alarmManager;
	private SharedPreferences prefs;
	private Editor prefsEditor;
	private static List<Integer> recurDays;
	
    public BootCompletedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d("MYNAMEISTODD", "Boot complete! Setting alarms...");
    	
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	prefs = context.getSharedPreferences("AUTOVOLUME", Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();
    	recurDays = new ArrayList<Integer>();

		Map<String, ?> allPrefs = prefs.getAll();
		for (String key : allPrefs.keySet()) {
			
			String[] timeRecur = key.split(":");
			
			Map<String, String> tmp = new HashMap<String, String>();
			tmp.put("TIME", timeRecur[0] + ":" + timeRecur[1]);
			tmp.put("RECUR", timeRecur[2]);
			tmp.put("VOLUME", (String) allPrefs.get(key));

			String[] recurDaysArray = tmp.get("RECUR").split("\\|");
			for (String rd : recurDaysArray) {
				if (rd.length() > 0) {
					int rdi = Integer.parseInt(rd);
					recurDays.add(rdi);
				}
			}
			
			final Calendar calNow = Calendar.getInstance();
			int hour = Integer.parseInt(timeRecur[0]);
			int minute = Integer.parseInt(timeRecur[1]);
			int nPickerVal = Integer.parseInt((String) allPrefs.get(key));
			
			for (int recurDay : recurDays) {
				
				if (recurDay != -1) {
					Calendar cNew = Calendar.getInstance();
					cNew.set(Calendar.DAY_OF_WEEK, recurDay+1);
					cNew.set(Calendar.HOUR_OF_DAY, hour);
					cNew.set(Calendar.MINUTE, minute);
					cNew.set(Calendar.SECOND, 0);
					if (cNew.before(calNow)) {
						cNew.roll(Calendar.WEEK_OF_YEAR, 1);
					}
					
					//Set alarms
					Intent intentBC = new Intent(context, SetAlarmManagerReceiver.class);
					String raw = "mnit://" + recurDay + "/" + hour + ":" + minute + "/" + nPickerVal;
					Uri data = Uri.parse(Uri.encode(raw));
					intentBC.setData(data);
					intentBC.putExtra("AUDIO_LEVEL", nPickerVal);
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentBC, PendingIntent.FLAG_UPDATE_CURRENT);
					alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cNew.getTimeInMillis(), 604800000, pendingIntent);
					
					Log.d("MYNAMEISTODD", "Time: " + DateUtils.formatDateTime(context, cNew.getTimeInMillis(), (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
				}
				else
				{
					Calendar cNew = Calendar.getInstance();
					cNew.set(Calendar.HOUR_OF_DAY, hour);
					cNew.set(Calendar.MINUTE, minute);
					cNew.set(Calendar.SECOND, 0);
					if (cNew.before(calNow)) {
						cNew.roll(Calendar.DAY_OF_WEEK, 1);
					}
					
					//Set one-time alarm
					Intent intentBC = new Intent(context, SetAlarmManagerReceiver.class);
					String raw = "mnit://" + (cNew.get(Calendar.DAY_OF_WEEK)-1) + "/" + hour + ":" + minute + "/" + nPickerVal;
					Uri data = Uri.parse(Uri.encode(raw));
					intentBC.setData(data);
					intentBC.putExtra("AUDIO_LEVEL", nPickerVal);
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentBC, PendingIntent.FLAG_UPDATE_CURRENT);
					alarmManager.set(AlarmManager.RTC_WAKEUP, cNew.getTimeInMillis(), pendingIntent);
					
					Log.d("MYNAMEISTODD", "Time: " + DateUtils.formatDateTime(context, cNew.getTimeInMillis(), (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
				}
			}
		}
    	
    }
}

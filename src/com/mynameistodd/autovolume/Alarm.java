package com.mynameistodd.autovolume;

import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.text.format.DateUtils;
import android.util.Log;

public class Alarm {

	private Editor prefsEditor;
	private AlarmManager alarmManager;
	
	private long id;
	private int hour;
	private int minute;
	private List<Integer> recur;
	private int volume;
	private boolean enabled;
	private Context context;
	public Alarm(long id, int hour, int minute, List<Integer> recur, int volume, boolean enabled, Context context) {
		super();
		this.id = id;
		this.hour = hour;
		this.minute = minute;
		this.recur = recur;
		this.volume = volume;
		this.enabled = enabled;
		this.context = context;
		
		prefsEditor = context.getSharedPreferences(Util.AUTOVOLUME, Context.MODE_PRIVATE).edit();
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}
	public long getId() {
		return id;
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinute() {
		return minute;
	}
	public void setMinute(int minute) {
		this.minute = minute;
	}
	public List<Integer> getRecur() {
		return recur;
	}
	public void setRecur(List<Integer> recur) {
		this.recur = recur;
	}
	public int getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		this.volume = volume;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		Log.d(Util.MYNAMEISTODD, "Enabled: " + enabled);
	}
	public void save() {
		prefsEditor.putString(String.valueOf(id), hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + ":" + enabled + ":" + String.valueOf(volume));
		Log.d(Util.MYNAMEISTODD, "Saved:" + hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + " Volume:" + String.valueOf(volume));
		prefsEditor.commit();
	}
	public void remove() {
		prefsEditor.remove(String.valueOf(id));
		Log.d(Util.MYNAMEISTODD, "Deleted:" + hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + " Volume:" + volume);
		prefsEditor.commit();
	}
	public void cancel() {
		for (int recurDay : recur) {
			PendingIntent pendingIntent = Util.createPendingIntent(context, hour, minute, volume, recurDay, enabled);
			alarmManager.cancel(pendingIntent);
		}
	}
	public void schedule() {
		for (int recurDay : recur) {
			final Calendar calNow = Calendar.getInstance();
			Calendar cNew = Calendar.getInstance();
			
			if (recurDay != -1) {
				cNew.set(Calendar.DAY_OF_WEEK, recurDay+1);
				cNew.set(Calendar.HOUR_OF_DAY, hour);
				cNew.set(Calendar.MINUTE, minute);
				cNew.set(Calendar.SECOND, 0);
				if (cNew.before(calNow)) {
					cNew.roll(Calendar.WEEK_OF_YEAR, 1);
				}
				
				//Set alarms
				PendingIntent pendingIntent = Util.createPendingIntent(context, hour, minute, volume, recurDay, enabled);
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cNew.getTimeInMillis(), 604800000, pendingIntent);
				
				Log.d(Util.MYNAMEISTODD, "Time: " + DateUtils.formatDateTime(context, cNew.getTimeInMillis(), (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
			}
			else
			{
				cNew.set(Calendar.HOUR_OF_DAY, hour);
				cNew.set(Calendar.MINUTE, minute);
				cNew.set(Calendar.SECOND, 0);
				if (cNew.before(calNow)) {
					cNew.roll(Calendar.DAY_OF_WEEK, 1);
				}
				
				//Set one-time alarm
				PendingIntent pendingIntent = Util.createPendingIntent(context, hour, minute, volume, (cNew.get(Calendar.DAY_OF_WEEK)-1), enabled);
				alarmManager.set(AlarmManager.RTC_WAKEUP, cNew.getTimeInMillis(), pendingIntent);
				
				Log.d(Util.MYNAMEISTODD, "Time: " + DateUtils.formatDateTime(context, cNew.getTimeInMillis(), (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
			}
		}
	}
}

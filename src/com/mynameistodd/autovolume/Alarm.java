package com.mynameistodd.autovolume;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class Alarm {

	private SharedPreferences prefs;
	private Editor prefsEditor;
	
	private long id;
	private int hour;
	private int minute;
	private List<Integer> recur;
	private int volume;
	private boolean enabled;
	public Alarm(long id, int hour, int minute, List<Integer> recur, int volume, boolean enabled, Context context) {
		super();
		this.id = id;
		this.hour = hour;
		this.minute = minute;
		this.recur = recur;
		this.volume = volume;
		this.enabled = enabled;
		
		prefsEditor = context.getSharedPreferences(Util.AUTOVOLUME, context.MODE_PRIVATE).edit();
	}
	public long getId() {
		return id;
	}
//	public void setId(long id) {
//		this.id = id;
//	}
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
		//prefsEditor.putString(hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + ":" + enabled, String.valueOf(volume));
		prefsEditor.putString(String.valueOf(id), hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + ":" + enabled + ":" + String.valueOf(volume));
		Log.d(Util.MYNAMEISTODD, "Saved:" + hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + " Volume:" + String.valueOf(volume));
		prefsEditor.commit();
	}
	public void remove() {
		//prefsEditor.remove(hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + ":" + enabled);
		prefsEditor.remove(String.valueOf(id));
		Log.d(Util.MYNAMEISTODD, "Deleted:" + hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + " Volume:" + volume);
		prefsEditor.commit();
	}
}

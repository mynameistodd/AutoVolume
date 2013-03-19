package com.mynameistodd.autovolume;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class Alarm {

	private SharedPreferences prefs;
	private Editor prefsEditor;
	
	private int hour;
	private int minute;
	private List<Integer> recur;
	private int volume;
	private boolean enabled;
	public Alarm(int hour, int minute, List<Integer> recur, int volume,	boolean enabled, Context context) {
		super();
		this.hour = hour;
		this.minute = minute;
		this.recur = recur;
		this.volume = volume;
		this.enabled = enabled;
		
		prefsEditor = context.getSharedPreferences(Util.AUTOVOLUME, context.MODE_PRIVATE).edit();
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
	}
	public void save() {
		prefsEditor.putString(hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|"), String.valueOf(volume));
		Log.d("MYNAMEISTODD", "Saved:" + hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + " Volume:" + String.valueOf(volume));
		prefsEditor.commit();
	}
	public void remove() {
		prefsEditor.remove(hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|"));
		Log.d("MYNAMEISTODD", "Deleted:" + hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + " Volume:" + volume);
		prefsEditor.commit();
	}
}

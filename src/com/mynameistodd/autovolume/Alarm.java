package com.mynameistodd.autovolume;

import java.util.List;

public class Alarm {

	private int hour;
	private int minute;
	private List<Integer> recur;
	private int volume;
	private boolean enabled;
	public Alarm(int hour, int minute, List<Integer> recur, int volume,
			boolean enabled) {
		super();
		this.hour = hour;
		this.minute = minute;
		this.recur = recur;
		this.volume = volume;
		this.enabled = enabled;
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
		
	}
	public void delete() {
		
	}
}

package com.mynameistodd.autovolume;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Alarm {

    private Context context;
    private AlarmManager alarmManager;
	private int id;
	private int hour;
	private int minute;
    private List<Integer> recur = new ArrayList<>();
    private int volume;
	private boolean enabled;
    private AlarmType type;
    private String title;
    private String instanceID;

    public Alarm(Context context) {
        Calendar cal = Calendar.getInstance();

        this.context = context;
        this.id = 0;
        this.hour = cal.get(Calendar.HOUR_OF_DAY);
        this.minute = cal.get(Calendar.MINUTE);
        this.recur.add(-1);
        this.volume = 0;
        this.enabled = true;
        this.type = AlarmType.Timed;

        init();
    }

    public Alarm(int id, int hour, int minute, List<Integer> recur, int volume, boolean enabled, AlarmType type, Context context) {
        super();
        this.context = context;

        this.id = id;
        this.hour = hour;
		this.minute = minute;
		this.recur = recur;
		this.volume = volume;
		this.enabled = enabled;
        this.type = type;

        init();
	}

    private void init() {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

	public int getId() {
		return id;
	}

    public void setId(int id) {
        this.id = id;
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

    public AlarmType getType() {
        return type;
    }

    public void setType(AlarmType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }

    public void save() {
        if (this.id <= 0) {
            schedule();
            MySQLiteOpenHelper.insertAlarm(context, this);
            Log.d(Util.MYNAMEISTODD, "Inserted:" + hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + " Volume:" + String.valueOf(volume) + " Enabled:" + String.valueOf(enabled));
        }
        else {
            schedule();
            MySQLiteOpenHelper.updateAlarm(context, this);
            Log.d(Util.MYNAMEISTODD, "Updated:" + hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + " Volume:" + String.valueOf(volume) + " Enabled:" + String.valueOf(enabled));
        }
	}

    public void delete() {
        this.cancel();
        MySQLiteOpenHelper.deleteAlarm(context, this);
        Log.d(Util.MYNAMEISTODD, "Deleted:" + hour + ":" + minute + ":" + Util.getRecurDelim(recur, "|") + " Volume:" + volume + "Enabled:" + enabled);
	}

    public void cancel() {
        if (recur != null) {
            for (int recurDay : recur) {
                PendingIntent pendingIntent = Util.createPendingIntent(context, hour, minute, volume, recurDay, enabled);
                alarmManager.cancel(pendingIntent);
            }
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

                Log.d(Util.MYNAMEISTODD, "Schedule-Repeating: " + DateUtils.formatDateTime(context, cNew.getTimeInMillis(), (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
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
				PendingIntent pendingIntent = Util.createPendingIntent(context, hour, minute, volume, -1, enabled);
				alarmManager.set(AlarmManager.RTC_WAKEUP, cNew.getTimeInMillis(), pendingIntent);

                Log.d(Util.MYNAMEISTODD, "Schedule-One-Time: " + DateUtils.formatDateTime(context, cNew.getTimeInMillis(), (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
			}
		}
	}

    public enum AlarmType {
        Timed,
        Calendar
    }
}

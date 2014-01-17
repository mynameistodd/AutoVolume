package com.mynameistodd.autovolume;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {

	static final String MYNAMEISTODD = "MYNAMEISTODD";
	
	public static PendingIntent createPendingIntent(Context context, int hour, int minute, int volume, int recurDay, boolean enabled) {
		Intent intent = new Intent(context, SetAlarmManagerReceiver.class);
		String raw = "mnit://" + recurDay + "/" + hour + ":" + minute + "/" + volume + "/" + enabled;
        Log.d(Util.MYNAMEISTODD, "PendingIntent: " + raw);
		Uri data = Uri.parse(Uri.encode(raw));
		intent.setData(data);
        intent.putExtra("HOUR", hour);
        intent.putExtra("MINUTE", minute);
		intent.putExtra("AUDIO_LEVEL", volume);
        intent.putExtra("ENABLED", enabled);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}
	public static String getVolumePercent(String volumeSet, Integer maxVolume)
	{
		float volume = Float.parseFloat(volumeSet);
		double toFormat = (volume / maxVolume.floatValue());
		return NumberFormat.getPercentInstance().format(toFormat);
	}
	public static String getRecurText(List<Integer> recurDays)
	{
		String textToShow = "";
		if (recurDays.size() > 0) {
			Collections.sort(recurDays);
			for (int recurDay : recurDays) {
					switch (recurDay) {
					case -1:
					default:
						textToShow = "One Time";
						break;
					case 0:
						textToShow += "Sun, ";
						break;
					case 1:
						textToShow += "Mon, ";
						break;
					case 2:
						textToShow += "Tue, ";
						break;
					case 3:
						textToShow += "Wed, ";
						break;
					case 4:
						textToShow += "Thu, ";
						break;
					case 5:
						textToShow += "Fri, ";
						break;
					case 6:
						textToShow += "Sat, ";
						break;
					}	
			}
			if (textToShow.equals("Mon, Tue, Wed, Thu, Fri, ")) {
				textToShow = "Weekdays";
			}
			else if (textToShow.equals("Sun, Mon, Tue, Wed, Thu, Fri, Sat, "))
			{
				textToShow = "Everyday";
			}
			else if (!textToShow.equals("One Time")) {
				textToShow = textToShow.substring(0, textToShow.length()-2);
			}
		}
		else
		{
			textToShow = "One Time";
			
		}
		return textToShow;
	}
	public static List<Integer> getRecurList(String recurText)
	{
		List<Integer> recurDays = new ArrayList<Integer>();
		String[] recurDaysArray = recurText.split("\\|");
		if (recurDaysArray.length > 0) {
			for (String recurDayStr : recurDaysArray) {
				if (recurDayStr.length() > 0) {
					int recurDay = Integer.parseInt(recurDayStr);
					recurDays.add(recurDay);
				}
			}
		}
		return recurDays;
	}
	public static String getRecurDelim(List<Integer> recurDays, String delimeter)
	{
		String returnText = delimeter;
		for (int i : recurDays) {
			returnText += i + delimeter;
		}
		return returnText;
	}
}

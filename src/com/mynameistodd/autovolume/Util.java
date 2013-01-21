package com.mynameistodd.autovolume;

import java.text.NumberFormat;
import java.util.List;

public class Util {

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
			if (textToShow != "One Time") {
				textToShow = textToShow.substring(0, textToShow.length()-2);
			}
		}
		else
		{
			textToShow = "One Time";
			
		}
		return textToShow;
	}
}

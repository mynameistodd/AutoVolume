package com.mynameistodd.autovolume;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.media.AudioManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class MyArrayAdapter extends ArrayAdapter<Alarm> {
	
	private Context context;
	private List<Alarm> alarms;
	private Integer maxVolume;

	public MyArrayAdapter(Context context, int textViewResourceId, List<Alarm> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.alarms = objects;
		this.maxVolume = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_RING);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
    	LayoutInflater inflater = (LayoutInflater)context.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
    	View rowView = inflater.inflate(R.layout.activity_list_alarm_item, null);
    	TextView time = (TextView) rowView.findViewById(R.id.tv_time);
    	TextView recur = (TextView) rowView.findViewById(R.id.tv_recur);
    	TextView volume = (TextView) rowView.findViewById(R.id.tv_volume);
    	CompoundButton enabled = (CompoundButton) rowView.findViewById(R.id.switch1);
	    
    	Alarm alarm = alarms.get(position);

    	//set the time
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, alarm.getHour());
		c.set(Calendar.MINUTE, alarm.getMinute());
		time.setText(DateUtils.formatDateTime(context, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
		
		//set the recur
		String textToShow = Util.getRecurText(alarm.getRecur());
		recur.setText(textToShow);
		
		//set the volume
		volume.setText(Util.getVolumePercent(String.valueOf(alarm.getVolume()), maxVolume));
		
		//set the on/off switch
		enabled.setChecked(alarm.isEnabled());
		enabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//alarm.setEnabled(isChecked);
			}
		});
	    
		return rowView;
	}

}

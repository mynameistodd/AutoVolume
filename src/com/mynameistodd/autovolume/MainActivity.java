package com.mynameistodd.autovolume;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

public class MainActivity extends FragmentActivity {

	private Button buttonSave;
	private Button buttonCancel;
	private AudioManager audioManager;
	private AlarmManager alarmManager;
	private static TextView time;
	private TimePickerDialog tPicker;
	private NumberPicker nPicker;
	private static int setHour = 0;
	private static int setMinute = 0;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        buttonSave = (Button)findViewById(R.id.button1);
        buttonCancel = (Button)findViewById(R.id.button2);
        time = (TextView)findViewById(R.id.textView2);
        nPicker = (NumberPicker)findViewById(R.id.numberPicker1);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
        final Calendar c = Calendar.getInstance();
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		int curMinute = c.get(Calendar.MINUTE);
		time.setText(DateUtils.formatDateTime(getApplicationContext(), c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
		
		int maxVolumeStep = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        nPicker.setMinValue(0);
        nPicker.setMaxValue(maxVolumeStep);
		
        tPicker = new TimePickerDialog(this, new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				
				final Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, hourOfDay);
				c.set(Calendar.MINUTE, minute);
				
				time.setText(DateUtils.formatDateTime(getApplicationContext(), c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
				
				setHour = hourOfDay;
				setMinute = minute;
			}
		}, curHour, curMinute, false);
		
        buttonSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int nPickerVal = nPicker.getValue();
				
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, setHour);
				c.set(Calendar.MINUTE, setMinute);
				c.set(Calendar.SECOND, 0);
				
				Intent intent = new Intent(getApplicationContext(), SetAlarmManagerReceiver.class);
				intent.putExtra("AUDIO_LEVEL", nPickerVal);
				
				PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				alarmManager.set(AlarmManager.RTC, c.getTimeInMillis(), pendingIntent);
				
				Intent returnIntent = new Intent();
				returnIntent.putExtra("HOUR", setHour);
				returnIntent.putExtra("MINUTE", setMinute);
				returnIntent.putExtra("VOLUME", nPickerVal);
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		});
        
        buttonCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
        
        time.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tPicker.show();
			}
		});
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}

package com.mynameistodd.autovolume;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateUtils;

public class EditCreateAlarm extends FragmentActivity {

	private SharedPreferences prefs;
	private Editor prefsEditor;
	private Button buttonSave;
	private Button buttonCancel;
	private AudioManager audioManager;
	private AlarmManager alarmManager;
	private static TextView time;
	private TimePickerDialog tPicker;
	private NumberPicker nPicker;
	private static int setHour = 0;
	private static int setMinute = 0;
	private Intent callingIntent;
	private TextView daysRecurring;
	private Context contextThis;
	private static List<String> recurDays;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_create_alarm);
        prefs = getSharedPreferences("AUTOVOLUME", MODE_PRIVATE);
		prefsEditor = prefs.edit();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        buttonSave = (Button)findViewById(R.id.button1);
        buttonCancel = (Button)findViewById(R.id.button2);
        time = (TextView)findViewById(R.id.textView2);
        nPicker = (NumberPicker)findViewById(R.id.numberPicker1);
        daysRecurring = (TextView)findViewById(R.id.textView4);
        contextThis = this;
        recurDays = new ArrayList<String>();
        
        int maxVolumeStep = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        nPicker.setMinValue(0);
        nPicker.setMaxValue(maxVolumeStep);
        
        callingIntent = getIntent();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
        final Calendar c = Calendar.getInstance();
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		int curMinute = c.get(Calendar.MINUTE);

		int hour = callingIntent.getIntExtra("HOUR", curHour);
		int minute = callingIntent.getIntExtra("MINUTE", curMinute);
		int volume = callingIntent.getIntExtra("VOLUME", 0);
		
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		
		time.setText(DateUtils.formatDateTime(getApplicationContext(), c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
		nPicker.setValue(volume);
		
		setHour = hour;
		setMinute = minute;
		
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
		}, hour, minute, false);
		
        buttonSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int nPickerVal = nPicker.getValue();
				
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, setHour);
				c.set(Calendar.MINUTE, setMinute);
				c.set(Calendar.SECOND, 0);
				
				//Delete old alarm
				Intent intentOld = new Intent(getApplicationContext(), SetAlarmManagerReceiver.class);
				String rawOld = "mnit://" + callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + "/" + callingIntent.getIntExtra("VOLUME", 0);
				Uri dataOld = Uri.parse(Uri.encode(rawOld));
				intentOld.setData(dataOld);
				intentOld.putExtra("AUDIO_LEVEL", callingIntent.getIntExtra("VOLUME", 0));
				PendingIntent pendingIntentOld = PendingIntent.getBroadcast(getApplicationContext(), 0, intentOld, PendingIntent.FLAG_UPDATE_CURRENT);
				alarmManager.cancel(pendingIntentOld);
				prefsEditor.remove(callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0));
				Log.d("MYNAMEISTODD", "Deleted:" + callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + " Volume:" + callingIntent.getIntExtra("VOLUME", 0));
				
				//Save new alarm
				Intent intent = new Intent(getApplicationContext(), SetAlarmManagerReceiver.class);
				String raw = "mnit://" + setHour + ":" + setMinute + "/" + nPickerVal;
				Uri data = Uri.parse(Uri.encode(raw));
				intent.setData(data);
				intent.putExtra("AUDIO_LEVEL", nPickerVal);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
				prefsEditor.putString(setHour + ":" + setMinute, String.valueOf(nPickerVal));
				Log.d("MYNAMEISTODD", "Saved:" + setHour + ":" + setMinute + " Volume:" + String.valueOf(nPickerVal));
				
				prefsEditor.commit();
				
				setResult(RESULT_OK);
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
        
        daysRecurring.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(0);
			}
		});
    }
	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {

		final CharSequence[] items = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

		AlertDialog.Builder builder = new AlertDialog.Builder(contextThis);
		builder.setTitle("Pick recuring days")
				.setCancelable(true)
				.setMultiChoiceItems(items, null, new OnMultiChoiceClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						//keep track of the selected items
						if (isChecked)
						{
							recurDays.add(items[which].toString());
						}
						else
						{
							if (recurDays.contains(items[which].toString()))
							{
								recurDays.remove(items[which].toString());
							}
						}
					}
				})
				.setPositiveButton("Done",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Log.d("MYNAMEISTODD", "Clicked Done");

							}
						})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Log.d("MYNAMEISTODD", "Clicked Cancel");
						dialog.cancel();
					}
				});

		AlertDialog alert = builder.create();
		return alert;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_edit_create_alarm, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.menu_delete:
			
			//Delete old alarm
			Intent intentOld = new Intent(getApplicationContext(), SetAlarmManagerReceiver.class);
			String rawOld = "mnit://" + callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + "/" + callingIntent.getIntExtra("VOLUME", 0);
			Uri dataOld = Uri.parse(Uri.encode(rawOld));
			intentOld.setData(dataOld);
			intentOld.putExtra("AUDIO_LEVEL", callingIntent.getIntExtra("VOLUME", 0));
			PendingIntent pendingIntentOld = PendingIntent.getBroadcast(getApplicationContext(), 0, intentOld, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.cancel(pendingIntentOld);
			prefsEditor.remove(callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0));
			
			prefsEditor.commit();
			
			setResult(RESULT_CANCELED);
			finish();
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
}

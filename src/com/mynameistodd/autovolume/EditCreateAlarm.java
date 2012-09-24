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
import android.content.DialogInterface.OnDismissListener;
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
	private static int hour = 0;
	private static int minute = 0;
	private static int volume = 0;
	private Intent callingIntent;
	private TextView daysRecurringLabel;
	private TextView daysRecurring;
	private Context contextThis;
	private static List<Integer> recurDays;
	private static boolean editMode = false;

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
        daysRecurringLabel = (TextView)findViewById(R.id.textView5);
        contextThis = this;
        recurDays = new ArrayList<Integer>();
        editMode = false;
        
        int maxVolumeStep = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        nPicker.setMinValue(0);
        nPicker.setMaxValue(maxVolumeStep);
        
        callingIntent = getIntent();
        if (callingIntent.hasExtra("HOUR") && callingIntent.hasExtra("MINUTE") && callingIntent.hasExtra("RECUR") && callingIntent.hasExtra("VOLUME"))
        {
        	editMode = true;
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
        Calendar c = Calendar.getInstance();
        
        hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		volume = 0;
		
        if (editMode)
        {
        	hour = callingIntent.getIntExtra("HOUR", hour);
        	minute = callingIntent.getIntExtra("MINUTE", minute);
        	volume = callingIntent.getIntExtra("VOLUME", volume);
        	String[] recurDaysArray = callingIntent.getStringExtra("RECUR").split("\\|");
			
			for (String rd : recurDaysArray) {
				if (!rd.isEmpty()) {
					int rdi = Integer.parseInt(rd);
					recurDays.add(rdi);
				}
			}
        }
		
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		
		time.setText(DateUtils.formatDateTime(getApplicationContext(), c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
		nPicker.setValue(volume);
		String textToShow = "";
		if (recurDays.size() > 0) {
			for (int recurDay : recurDays) {
					switch (recurDay) {
					case 0:
						textToShow += "Sun,";
						break;
					case 1:
						textToShow += "Mon,";
						break;
					case 2:
						textToShow += "Tue,";
						break;
					case 3:
						textToShow += "Wed,";
						break;
					case 4:
						textToShow += "Thu,";
						break;
					case 5:
						textToShow += "Fri,";
						break;
					case 6:
						textToShow += "Sat,";
						break;
					default:
						break;
					}	
			}
			textToShow = textToShow.substring(0, textToShow.length()-1);
		}
		daysRecurring.setText(textToShow);
		
        tPicker = new TimePickerDialog(this, new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
				
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, hourOfDay);
				c.set(Calendar.MINUTE, minuteOfDay);
				
				time.setText(DateUtils.formatDateTime(getApplicationContext(), c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
				
				hour = hourOfDay;
				minute = minuteOfDay;
			}
		}, hour, minute, false);
		
        buttonSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int nPickerVal = nPicker.getValue();
				
				//Delete old alarm
				if (editMode) {
					String[] recurDaysArray = callingIntent.getStringExtra("RECUR").split("\\|");
					
					//Cancel the alarms
					for (String recurDayStr : recurDaysArray) {
						if (!recurDayStr.isEmpty()) {
							int recurDay = Integer.parseInt(recurDayStr);
							
							Intent intentOld = new Intent(getApplicationContext(), SetAlarmManagerReceiver.class);
							String rawOld = "mnit://" + recurDay + "/" + callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + "/" + callingIntent.getIntExtra("VOLUME", 0);
							Uri dataOld = Uri.parse(Uri.encode(rawOld));
							intentOld.setData(dataOld);
							intentOld.putExtra("AUDIO_LEVEL", callingIntent.getIntExtra("VOLUME", 0));
							PendingIntent pendingIntentOld = PendingIntent.getBroadcast(getApplicationContext(), 0, intentOld, PendingIntent.FLAG_UPDATE_CURRENT);
							alarmManager.cancel(pendingIntentOld);
						}
					}
					prefsEditor.remove(callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + ":" + callingIntent.getStringExtra("RECUR"));
					Log.d("MYNAMEISTODD", "Deleted:" + callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + ":" + callingIntent.getStringExtra("RECUR") + " Volume:" + callingIntent.getIntExtra("VOLUME", 0));
				}
				
				//Save new alarm
				String recurDaysDelim = "|";
				for (int recurDay : recurDays) {
					recurDaysDelim += recurDay + "|";
					
					Calendar cNew = Calendar.getInstance();
					cNew.set(Calendar.DAY_OF_WEEK, recurDay+1);
					cNew.set(Calendar.HOUR_OF_DAY, hour);
					cNew.set(Calendar.MINUTE, minute);
					cNew.set(Calendar.SECOND, 0);
					
					//Set alarms
					Intent intent = new Intent(getApplicationContext(), SetAlarmManagerReceiver.class);
					String raw = "mnit://" + recurDay + "/" + hour + ":" + minute + "/" + nPickerVal;
					Uri data = Uri.parse(Uri.encode(raw));
					intent.setData(data);
					intent.putExtra("AUDIO_LEVEL", nPickerVal);
					PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cNew.getTimeInMillis(), 604800000, pendingIntent);
					
				}
				prefsEditor.putString(hour + ":" + minute + ":" + recurDaysDelim, String.valueOf(nPickerVal));
				Log.d("MYNAMEISTODD", "Saved:" + hour + ":" + minute + ":" + recurDaysDelim + " Volume:" + String.valueOf(nPickerVal));
				
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
        
        daysRecurringLabel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(0);
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
		boolean[] itemsChecked = new boolean[7];
		
		if (editMode) {
			String[] recurDaysArray = callingIntent.getStringExtra("RECUR").split("\\|");
			
			for (String rd : recurDaysArray) {
				if (!rd.isEmpty()) {
					int rdi = Integer.parseInt(rd);
					itemsChecked[rdi] = true;
					recurDays.add(rdi);
				}
			}
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(contextThis);
		builder.setTitle("Pick recurring days")
				.setCancelable(true)
				.setMultiChoiceItems(items, itemsChecked, new OnMultiChoiceClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						//keep track of the selected items
						if (isChecked)
						{
							if (!recurDays.contains(which))
							{
								recurDays.add(which);
							}
						}
						else
						{
							if (recurDays.contains(which))
							{
								recurDays.remove(recurDays.indexOf(which));
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
		alert.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				String textToShow = "";
				if (recurDays.size() > 0) {
					for (int recurDay : recurDays) {
							switch (recurDay) {
							case 0:
								textToShow += "Sun,";
								break;
							case 1:
								textToShow += "Mon,";
								break;
							case 2:
								textToShow += "Tue,";
								break;
							case 3:
								textToShow += "Wed,";
								break;
							case 4:
								textToShow += "Thu,";
								break;
							case 5:
								textToShow += "Fri,";
								break;
							case 6:
								textToShow += "Sat,";
								break;
							default:
								break;
							}	
					}
					textToShow = textToShow.substring(0, textToShow.length()-1);
				}
				daysRecurring.setText(textToShow);
			}
		});
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
			if (editMode) {
				String[] recurDaysArray = callingIntent.getStringExtra("RECUR").split("\\|");
				
				//Cancel the alarms
				for (String recurDayStr : recurDaysArray) {
					if (!recurDayStr.isEmpty()) {
						int recurDay = Integer.parseInt(recurDayStr);
						
						Intent intentOld = new Intent(getApplicationContext(), SetAlarmManagerReceiver.class);
						String rawOld = "mnit://" + recurDay + "/" + callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + "/" + callingIntent.getIntExtra("VOLUME", 0);
						Uri dataOld = Uri.parse(Uri.encode(rawOld));
						intentOld.setData(dataOld);
						intentOld.putExtra("AUDIO_LEVEL", callingIntent.getIntExtra("VOLUME", 0));
						PendingIntent pendingIntentOld = PendingIntent.getBroadcast(getApplicationContext(), 0, intentOld, PendingIntent.FLAG_UPDATE_CURRENT);
						alarmManager.cancel(pendingIntentOld);
					}
				}
				prefsEditor.remove(callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + ":" + callingIntent.getStringExtra("RECUR"));
				Log.d("MYNAMEISTODD", "Deleted:" + callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + ":" + callingIntent.getStringExtra("RECUR") + " Volume:" + callingIntent.getIntExtra("VOLUME", 0));
			}
			
			prefsEditor.commit();
			
			setResult(RESULT_OK);
			finish();
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
}

package com.mynameistodd.autovolume;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TableRow;
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
	private static TextView tvTime;
//	private static ImageView arrowUp;
//	private static ImageView arrowDown;
	private static TableRow timeTableRow;
	private static TableRow volumeTableRow;
	private static TableRow recurTableRow;
	private TimePickerDialog tPicker;
	private static Calendar cal;
	private static int hour = 0;
	private static int minute = 0;
	private static int volume = 0;
	private Intent callingIntent;
	//private TextView tvRecurLabel;
	private TextView tvRecur;
	private Context contextThis;
	private static List<Integer> recurDays;
	private static boolean editMode = false;
	private SeekBar seekBar;
	private TextView tvVolume;
	private int maxVolumeStep;
	private int nPickerVal = 0;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_create_alarm);
        contextThis = this;
        prefs = getSharedPreferences(Util.AUTOVOLUME, MODE_PRIVATE);
		prefsEditor = prefs.edit();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        buttonSave = (Button)findViewById(R.id.btnSave);
        buttonCancel = (Button)findViewById(R.id.btnCancel);
        tvTime = (TextView)findViewById(R.id.tvTime);
        //arrowUp = (ImageView)findViewById(R.id.imageView1);
        //arrowDown = (ImageView)findViewById(R.id.imageView2);
        timeTableRow = (TableRow)findViewById(R.id.timeTableRow);
        volumeTableRow = (TableRow)findViewById(R.id.volumeTableRow);
        recurTableRow = (TableRow)findViewById(R.id.recurTableRow);
        seekBar = new SeekBar(contextThis);
        tvRecur = (TextView)findViewById(R.id.tvRecur);
        //tvRecurLabel = (TextView)findViewById(R.id.tvRecurLabel);
        tvVolume = (TextView)findViewById(R.id.tvVolume);
        recurDays = new ArrayList<Integer>();
        editMode = false;
        
        maxVolumeStep = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        seekBar.setMax(maxVolumeStep);
        
        callingIntent = getIntent();
        if (callingIntent.hasExtra("HOUR") && callingIntent.hasExtra("MINUTE") && callingIntent.hasExtra("RECUR") && callingIntent.hasExtra("VOLUME"))
        {
        	editMode = true;
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
        cal = Calendar.getInstance();
        
        hour = cal.get(Calendar.HOUR_OF_DAY);
		minute = cal.get(Calendar.MINUTE);
		volume = 0;
		recurDays.clear();
		
        if (editMode)
        {
        	hour = callingIntent.getIntExtra("HOUR", hour);
        	minute = callingIntent.getIntExtra("MINUTE", minute);
        	volume = callingIntent.getIntExtra("VOLUME", volume);
        	//String[] recurDaysArray = callingIntent.getStringExtra("RECUR").split("\\|");
        	List<Integer> recurDaysArray = callingIntent.getIntegerArrayListExtra("RECUR");
			
			for (int rd : recurDaysArray) {
				//if (rd.length() > 0) {
					//int rdi = Integer.parseInt(rd);
					recurDays.add(rd);
				//}
			}
        }
        else
        {
        	if (!recurDays.contains(-1)) {
				recurDays.add(-1);
			}
        }
		
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		
		tvTime.setText(DateUtils.formatDateTime(getApplicationContext(), cal.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
		tvVolume.setText(Util.getVolumePercent(Integer.toString(volume), maxVolumeStep));
		seekBar.setProgress(volume);
		
		String textToShow = Util.getRecurText(recurDays);
		
		tvRecur.setText(textToShow);
		
        tPicker = new TimePickerDialog(this, new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
				
				//Calendar c = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				cal.set(Calendar.MINUTE, minuteOfDay);
				
				tvTime.setText(DateUtils.formatDateTime(getApplicationContext(), cal.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
				
				hour = hourOfDay;
				minute = minuteOfDay;
			}
		}, hour, minute, false);
		
        buttonSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//Delete old alarm
				if (editMode) {
					//String[] recurDaysArray = callingIntent.getStringExtra("RECUR").split("\\|");
					List<Integer> recurDaysArray = callingIntent.getIntegerArrayListExtra("RECUR");
					
					//Cancel the alarms
					for (int recurDayStr : recurDaysArray) {
						//if (recurDayStr.length() > 0) {
							//int recurDay = Integer.parseInt(recurDayStr);
							
							PendingIntent pendingIntent = Util.createPendingIntent(getApplicationContext(), callingIntent.getIntExtra("HOUR", 0), callingIntent.getIntExtra("MINUTE", 0), callingIntent.getIntExtra("VOLUME", 0), recurDayStr);
							alarmManager.cancel(pendingIntent);
						//}
					}
					prefsEditor.remove(callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + ":" + Util.getRecurDelim(callingIntent.getIntegerArrayListExtra("RECUR"), "|"));
					Log.d("MYNAMEISTODD", "Deleted:" + callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + ":" + Util.getRecurDelim(callingIntent.getIntegerArrayListExtra("RECUR"), "|") + " Volume:" + callingIntent.getIntExtra("VOLUME", 0));
				}
				
				final Calendar calNow = Calendar.getInstance();
				//Save new alarm
				if (recurDays.size() > 0) {
					String recurDaysDelim = "|";
					for (int recurDay : recurDays) {
						recurDaysDelim += recurDay + "|";
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
							PendingIntent pendingIntent = Util.createPendingIntent(getApplicationContext(), hour, minute, nPickerVal, recurDay);
							alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cNew.getTimeInMillis(), 604800000, pendingIntent);
							
							Log.d("MYNAMEISTODD", "Time: " + DateUtils.formatDateTime(getApplicationContext(), cNew.getTimeInMillis(), (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
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
							PendingIntent pendingIntent = Util.createPendingIntent(getApplicationContext(), hour, minute, nPickerVal, (cNew.get(Calendar.DAY_OF_WEEK)-1));
							alarmManager.set(AlarmManager.RTC_WAKEUP, cNew.getTimeInMillis(), pendingIntent);
							
							Log.d("MYNAMEISTODD", "Time: " + DateUtils.formatDateTime(getApplicationContext(), cNew.getTimeInMillis(), (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
						}
					}
					prefsEditor.putString(hour + ":" + minute + ":" + recurDaysDelim, String.valueOf(nPickerVal));
					Log.d("MYNAMEISTODD", "Saved:" + hour + ":" + minute + ":" + recurDaysDelim + " Volume:" + String.valueOf(nPickerVal));
					
				}
				
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
        
//        tvTime.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				tPicker.show();
//			}
//		});
        
//        tvVolume.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				showDialog(1);
//			}
//		});
        
//		arrowUp.setOnClickListener(new OnClickListener() {
//					
//			@Override
//			public void onClick(View v) {
//				tPicker.show();
//			}
//		});
//		
//		arrowDown.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				tPicker.show();
//			}
//		});
        
//        tvRecurLabel.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				showDialog(0);
//			}
//		});
//        
//        tvRecur.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				showDialog(0);
//			}
//		});
		
		timeTableRow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tPicker.show();
			}
		});
        
		volumeTableRow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(1);
			}
		});
		
		recurTableRow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(0);
			}
		});
    }
	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		if (id == 0) {
		
			final CharSequence[] items = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
			boolean[] itemsChecked = new boolean[7];
			
			if (editMode) {
				for (int rd : recurDays) {
					if (rd >= 0) {
						itemsChecked[rd] = true;
					}
				}
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(contextThis);
			builder.setTitle("Pick recurring days")
					.setMultiChoiceItems(items, itemsChecked, new OnMultiChoiceClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							//keep track of the selected items
							if (isChecked)
							{
								if (!recurDays.contains(which))
								{
									recurDays.add(which);
									Log.d("MYNAMEISTODD", "Add:" + which);
								}
								
								if (recurDays.contains(-1)) {
									recurDays.remove(recurDays.indexOf(-1)); //added a recur day, remove "one time"
									Log.d("MYNAMEISTODD", "Remove:-1");
								}
							}
							else
							{
								if (recurDays.contains(which))
								{
									recurDays.remove(recurDays.indexOf(which));
									Log.d("MYNAMEISTODD", "Remove:" + which);
								}
							}
							
							if (recurDays.size() == 0) {
								recurDays.add(-1);
								Log.d("MYNAMEISTODD", "Add:-1");
							}
						}
					})
					.setPositiveButton("Done",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									Log.d("MYNAMEISTODD", "Clicked Done in PickDays");
								}
							});
	
			AlertDialog alert = builder.create();
			alert.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (recurDays.size() == 0)
					{
						if (!recurDays.contains(-1)) {
							recurDays.add(-1);
						}
					}
					
					String textToShow = Util.getRecurText(recurDays);
					tvRecur.setText(textToShow);
				}
			});
			return alert;
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(contextThis);
			builder.setTitle("Set Volume").setPositiveButton("Set",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Log.d("MYNAMEISTODD", "Clicked Done in SetVolume");
						}
					})
					.setView(seekBar);
			
			AlertDialog alert = builder.create();
			alert.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					nPickerVal = seekBar.getProgress();
					tvVolume.setText(Util.getVolumePercent(Integer.toString(nPickerVal), maxVolumeStep));
					Log.d("MYNAMEISTODD", "SetVolume" + nPickerVal);
				}
			});
			return alert;
		}
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
				//String[] recurDaysArray = callingIntent.getStringExtra("RECUR").split("\\|");
				List<Integer> recurDaysArray = callingIntent.getIntegerArrayListExtra("RECUR");
				
				//Cancel the alarms
				for (int recurDayStr : recurDaysArray) {
					//if (recurDayStr.length() > 0) {
						//int recurDay = Integer.parseInt(recurDayStr);
						
						PendingIntent pendingIntent = Util.createPendingIntent(getApplicationContext(), callingIntent.getIntExtra("HOUR", 0), callingIntent.getIntExtra("MINUTE", 0), callingIntent.getIntExtra("VOLUME", 0), recurDayStr);
						alarmManager.cancel(pendingIntent);
					//}
				}
				prefsEditor.remove(callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + ":" + Util.getRecurDelim(callingIntent.getIntegerArrayListExtra("RECUR"), "|"));
				Log.d("MYNAMEISTODD", "Deleted:" + callingIntent.getIntExtra("HOUR", 0) + ":" + callingIntent.getIntExtra("MINUTE", 0) + ":" + Util.getRecurDelim(callingIntent.getIntegerArrayListExtra("RECUR"), "|") + " Volume:" + callingIntent.getIntExtra("VOLUME", 0));
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

package com.mynameistodd.autovolume;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateUtils;

import com.google.ads.*;

public class ListAlarms extends ListActivity {

	private SharedPreferences prefs;
	private Editor prefsEditor;
	private Button btnAdd;
	private Context context;
	private Context contextThis;
	private ListView list;
	private static Map<String, ?> itemToDelete;
	//private static List<Map<String, ?>> listMapLocal;
	//private SimpleAdapter sa;
	private AudioManager audioManager;
	private AlarmManager alarmManager;
	private AdView adView;
	private Integer maxVolume;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_alarms);
		prefs = getSharedPreferences(Util.AUTOVOLUME, MODE_PRIVATE);
		prefsEditor = prefs.edit();
		btnAdd = (Button) findViewById(R.id.btn_add_new);
		context = getApplicationContext();
		contextThis = this;
		list = getListView();
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		adView = new AdView(this, AdSize.SMART_BANNER, "a150719f918826b");
		
		maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.RelativeLayout1);
		layout.addView(adView);
		AdRequest adRequest = new AdRequest();
		//adRequest.addTestDevice("BEC95AF7414ACCA7627A9C768544EB30");
		//adRequest.addTestDevice("3D173FEA54EE7C5D1C062C644D79DAF5");
		//adRequest.addTestDevice(AdRequest.TEST_EMULATOR);

		adView.loadAd(adRequest);

		btnAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, EditCreateAlarm.class);
				startActivityForResult(intent, 1);
			}
		});

		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@SuppressWarnings({ "deprecation", "unchecked" })
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				itemToDelete = (HashMap<String, ?>) arg0.getItemAtPosition(arg2);

				showDialog(0);
				return true;
			}

		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		//listMapLocal = new ArrayList<Map<String, ?>>();
		List<Alarm> allAlarms = new ArrayList<Alarm>();

		Map<String, ?> allPrefs = prefs.getAll();
		for (String key : allPrefs.keySet()) {
			
			String[] timeRecur = key.split(":");
			
//			Map<String, Object> tmp = new HashMap<String, Object>();
//			tmp.put("TIME", timeRecur[0] + ":" + timeRecur[1]);
//			tmp.put("RECUR", timeRecur[2]);
//			tmp.put("VOLUME", (String) allPrefs.get(key));
//			tmp.put("ENABLED", true);
//			listMapLocal.add(tmp);
			
			List<Integer> rd = Util.getRecurList((String) allPrefs.get(key));
			Alarm newAlarm = new Alarm(Integer.parseInt(timeRecur[0]), Integer.parseInt(timeRecur[1]), rd, Integer.parseInt((String) allPrefs.get(key)), true);
			allAlarms.add(newAlarm);
		}
//		Collections.sort(listMapLocal, new Comparator<Map<String,?>>() {
//
//			@Override
//			public int compare(Map<String, ?> lhs, Map<String, ?> rhs) {
//				int hour1 = Integer.parseInt(lhs.get("TIME").toString().substring(0, lhs.get("TIME").toString().indexOf(":")));
//				int hour2 = Integer.parseInt(rhs.get("TIME").toString().substring(0, rhs.get("TIME").toString().indexOf(":")));
//				return (hour1 == hour2) ? 0 : (hour1 > hour2) ? 1 : -1;
//			}
//		});

		ArrayAdapter<Alarm> adapter = new MyArrayAdapter(this, R.layout.activity_list_alarm_item, allAlarms);
		
//		sa = new SimpleAdapter(context, listMapLocal,
//				R.layout.activity_list_alarm_item, new String[] { "TIME", "RECUR", "VOLUME", "ENABLED" }, new int[] { R.id.tv_time, R.id.tv_recur, R.id.tv_volume, R.id.switch1 }) {
//			@Override
//			public void setViewText(TextView v, String text) {
//				super.setViewText(v, text);
//				if (v.getId() == R.id.tv_time) {
//					String[] hourMinute = text.split(":");
//					int hour = Integer.valueOf(hourMinute[0]);
//					int minute = Integer.valueOf(hourMinute[1]);
//
//					Calendar c = Calendar.getInstance();
//					c.set(Calendar.HOUR_OF_DAY, hour);
//					c.set(Calendar.MINUTE, minute);
//
//					v.setText(DateUtils.formatDateTime(context,
//							c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
//				}
//				else if (v.getId() == R.id.tv_recur) {
//					
//					List<Integer> recurDays = new ArrayList<Integer>();
//					String[] recurDaysArray = text.split("\\|");
//					if (recurDaysArray.length > 0) {
//						for (String recurDayStr : recurDaysArray) {
//							if (recurDayStr.length() > 0) {
//								int recurDay = Integer.parseInt(recurDayStr);
//								recurDays.add(recurDay);
//							}
//						}
//					}
//					
//					String textToShow = Util.getRecurText(recurDays);
//					v.setText(textToShow);
//				}
//				else if (v.getId() == R.id.tv_volume) {
//					v.setText(Util.getVolumePercent(text, maxVolume));
//				}
//			}
//		};
//		sa.setViewBinder(new MyViewBinder());
		
		//setListAdapter(sa);
		setListAdapter(adapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {

			// put in a Toast here that it was saved.
			Toast.makeText(contextThis, "Saved!", Toast.LENGTH_SHORT).show();
			//sa.notifyDataSetChanged();
			requestBackup();
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		//SimpleAdapter sa = (SimpleAdapter) l.getAdapter();
		MyArrayAdapter sa = (MyArrayAdapter) l.getAdapter();
		//Map<String, String> item = (Map<String, String>) sa.getItem(position);
		Alarm item = sa.getItem(position);

		//String[] time = item.get("TIME").split(":");

		Intent intent = new Intent(context, EditCreateAlarm.class);
//		intent.putExtra("HOUR", Integer.parseInt(time[0]));
//		intent.putExtra("MINUTE", Integer.parseInt(time[1]));
//		intent.putExtra("RECUR", item.get("RECUR"));
//		intent.putExtra("VOLUME", Integer.parseInt(item.get("VOLUME")));
		intent.putExtra("HOUR", item.getHour());
		intent.putExtra("MINUTE", item.getMinute());
		intent.putExtra("RECUR", item.getRecur().toArray());
		intent.putExtra("VOLUME", item.getVolume());

		startActivityForResult(intent, 1);

		Log.d("MYNAMEISTODD", "Position:" + position);
		Log.d("MYNAMEISTODD", "ID:" + id);
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(contextThis);
		builder.setMessage(R.string.delete_this_schedule)
				.setCancelable(true)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Log.d("MYNAMEISTODD", "Clicked Yes");

								//Delete old alarm
								String[] recurDaysArray = ((String) itemToDelete.get("RECUR")).split("\\|");
								
								//Cancel the alarms
								for (String recurDayStr : recurDaysArray) {
									if (recurDayStr.length() > 0) {
										int recurDay = Integer.parseInt(recurDayStr);
										
										String[] hourMin = ((String) itemToDelete.get("TIME")).split(":");
										
										PendingIntent pendingIntent = Util.createPendingIntent(context, Integer.parseInt(hourMin[0]), Integer.parseInt(hourMin[1]), Integer.parseInt((String) itemToDelete.get("VOLUME")), recurDay);
										alarmManager.cancel(pendingIntent);
									}
								}
								prefsEditor.remove(itemToDelete.get("TIME") + ":" + itemToDelete.get("RECUR"));
								Log.d("MYNAMEISTODD", "Deleted:" + itemToDelete.get("TIME") + ":" + itemToDelete.get("RECUR") + " Volume:" + itemToDelete.get("VOLUME"));

								prefsEditor.commit();

								//listMapLocal.remove(itemToDelete);
								//sa.notifyDataSetChanged();
								requestBackup();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Log.d("MYNAMEISTODD", "Clicked No");
						dialog.cancel();
					}
				});

		AlertDialog alert = builder.create();
		return alert;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_list_alarms, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(context, SettingsActivity.class));
			return true;
		case R.id.delete_all_prefs:
			prefsEditor.clear().commit();
			//listMapLocal.clear();
			//sa.notifyDataSetChanged();
			requestBackup();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	public void onDestroy() {
	  if (adView != null) {
	    adView.destroy();
	  }
	  super.onDestroy();
	}
	
	public void requestBackup()
	{
		BackupManager bm = new BackupManager(this);
		bm.dataChanged();
	}
	
//	class MyViewBinder implements ViewBinder {
//
//		@SuppressLint("NewApi")
//		@Override
//		public boolean setViewValue(View view, Object data,	String textRepresentation) {
//			if (view.getClass().equals(Switch.class))
//			{
//				CompoundButton switchToggle = (CompoundButton)view;
//				switchToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//					
//					@Override
//					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//						boolean checked = isChecked;
//					}
//				});
//				return true;
//			}
//			return false;
//		}
//		
//	}
}

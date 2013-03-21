package com.mynameistodd.autovolume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.ads.*;

public class ListAlarms extends ListActivity {

	private SharedPreferences prefs;
	private Editor prefsEditor;
	private Button btnAdd;
	private Context context;
	private Context contextThis;
	private ListView list;
	private Alarm alarmToDelete;
	private AlarmManager alarmManager;
	private AdView adView;
	private ArrayAdapter<Alarm> adapter;
	private List<Alarm> allAlarms;

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
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		adView = new AdView(this, AdSize.SMART_BANNER, "a150719f918826b");
		
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

			@SuppressWarnings({ "deprecation" })
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				alarmToDelete = (Alarm) arg0.getItemAtPosition(arg2);

				showDialog(0);
				return true;
			}

		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		allAlarms = new ArrayList<Alarm>();

		Map<String, ?> allPrefs = prefs.getAll();
		for (String key : allPrefs.keySet()) {
			
			String[] timeRecur = key.split(":");
			
			List<Integer> rd = Util.getRecurList(timeRecur[2]);
			Alarm newAlarm = new Alarm(Integer.parseInt(timeRecur[0]), Integer.parseInt(timeRecur[1]), rd, Integer.parseInt((String) allPrefs.get(key)), true, this);
			allAlarms.add(newAlarm);
		}
		Collections.sort(allAlarms, new Comparator<Alarm>() {

			@Override
			public int compare(Alarm lhs, Alarm rhs) {
				int hour1 = lhs.getHour();
				int hour2 = rhs.getHour();
				return (hour1 == hour2) ? 0 : (hour1 > hour2) ? 1 : -1;
			}
		});

		adapter = new MyArrayAdapter(this, R.layout.activity_list_alarm_item, allAlarms);
		setListAdapter(adapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {

			// put in a Toast here that it was saved.
			Toast.makeText(contextThis, "Saved!", Toast.LENGTH_SHORT).show();
			adapter.notifyDataSetChanged();
			requestBackup();
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		MyArrayAdapter adapter = (MyArrayAdapter) l.getAdapter();
		Alarm item = adapter.getItem(position);

		Intent intent = new Intent(context, EditCreateAlarm.class);
		intent.putExtra("HOUR", item.getHour());
		intent.putExtra("MINUTE", item.getMinute());
		intent.putIntegerArrayListExtra("RECUR", (ArrayList<Integer>) item.getRecur());
		intent.putExtra("VOLUME", item.getVolume());
		intent.putExtra("ENABLED", item.isEnabled());

		startActivityForResult(intent, 1);

		Log.d(Util.MYNAMEISTODD, "Position:" + position);
		Log.d(Util.MYNAMEISTODD, "ID:" + id);
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
								Log.d(Util.MYNAMEISTODD, "Clicked Yes");

								//Delete old alarms
								List<Integer> recurDaysArray = alarmToDelete.getRecur();
								
								//Cancel the alarms
								for (int recurDay : recurDaysArray) {
									PendingIntent pendingIntent = Util.createPendingIntent(context, alarmToDelete.getHour(), alarmToDelete.getMinute(), alarmToDelete.getVolume(), recurDay);
									alarmManager.cancel(pendingIntent);
								}
								prefsEditor.remove(alarmToDelete.getHour() + ":" + alarmToDelete.getMinute() + ":" + Util.getRecurDelim(alarmToDelete.getRecur(), "|"));
								Log.d(Util.MYNAMEISTODD, "Deleted:" + alarmToDelete.getHour() + ":" + alarmToDelete.getMinute() + ":" + Util.getRecurDelim(alarmToDelete.getRecur(), "|") + " Volume:" + alarmToDelete.getVolume());

								prefsEditor.commit();

								allAlarms.remove(alarmToDelete);
								adapter.notifyDataSetChanged();
								requestBackup();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Log.d(Util.MYNAMEISTODD, "Clicked No");
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
			allAlarms.clear();
			adapter.notifyDataSetChanged();
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
}

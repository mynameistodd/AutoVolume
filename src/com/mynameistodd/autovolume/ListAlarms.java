package com.mynameistodd.autovolume;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateUtils;

public class ListAlarms extends ListActivity {

	private SharedPreferences prefs;
	private Editor prefsEditor;
	private Button btnAdd;
	private Context context;
	private Context contextThis;
	private ListView list;
	private static Map<String, ?> itemToDelete;
	private static List<Map<String, ?>> listMapLocal;
	private SimpleAdapter sa;
	private AlarmManager alarmManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_alarms);
		prefs = getSharedPreferences("AUTOVOLUME", MODE_PRIVATE);
		prefsEditor = prefs.edit();
		btnAdd = (Button) findViewById(R.id.btn_add_new);
		context = getApplicationContext();
		contextThis = this;
		list = getListView();
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

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

		listMapLocal = new ArrayList<Map<String, ?>>();

		Map<String, ?> allPrefs = prefs.getAll();
		for (String key : allPrefs.keySet()) {

			Map<String, String> tmp = new HashMap<String, String>();
			tmp.put("TIME", key);
			tmp.put("VOLUME", (String) allPrefs.get(key));
			listMapLocal.add(tmp);

		}

		sa = new SimpleAdapter(context, listMapLocal,
				R.layout.activity_list_alarm_item, new String[] { "TIME",
						"VOLUME" }, new int[] { R.id.tv_time, R.id.tv_volume }) {
			@Override
			public void setViewText(TextView v, String text) {
				super.setViewText(v, text);
				if (v.getId() == R.id.tv_time) {
					String[] hourMinute = text.split(":");
					int hour = Integer.valueOf(hourMinute[0]);
					int minute = Integer.valueOf(hourMinute[1]);

					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, hour);
					c.set(Calendar.MINUTE, minute);

					v.setText(DateUtils.formatDateTime(context,
							c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
				} else if (v.getId() == R.id.tv_volume) {
					v.setText(text);
				}
			}
		};
		setListAdapter(sa);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {

			// put in a Toast here that it was saved.
			Toast.makeText(contextThis, "Saved!", Toast.LENGTH_SHORT).show();
			sa.notifyDataSetChanged();
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		SimpleAdapter sa = (SimpleAdapter) l.getAdapter();
		Map<String, String> item = (Map<String, String>) sa.getItem(position);

		String[] time = item.get("TIME").split(":");

		Intent intent = new Intent(context, EditCreateAlarm.class);
		intent.putExtra("HOUR", Integer.parseInt(time[0]));
		intent.putExtra("MINUTE", Integer.parseInt(time[1]));
		intent.putExtra("VOLUME", Integer.parseInt(item.get("VOLUME")));

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

								String time = (String) itemToDelete.get("TIME");
								int volume = Integer.parseInt((String) itemToDelete.get("VOLUME"));
								
								Intent intentOld = new Intent(getApplicationContext(), SetAlarmManagerReceiver.class);
								String rawOld = "mnit://" + time + "/" + volume;
								Uri dataOld = Uri.parse(Uri.encode(rawOld));
								intentOld.setData(dataOld);
								intentOld.putExtra("AUDIO_LEVEL", volume);
								PendingIntent pendingIntentOld = PendingIntent.getBroadcast(getApplicationContext(), 0, intentOld, PendingIntent.FLAG_UPDATE_CURRENT);
								alarmManager.cancel(pendingIntentOld);
								prefsEditor.remove(time);
								
								prefsEditor.commit();

								listMapLocal.remove(itemToDelete);
								sa.notifyDataSetChanged();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Log.d("MYNAMEISTODD", "Clicked Yes");
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

}

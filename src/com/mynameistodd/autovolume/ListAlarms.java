package com.mynameistodd.autovolume;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.integer;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.text.format.DateUtils;

public class ListAlarms extends ListActivity {

	private SharedPreferences prefs;
	private Editor prefsEditor;
	private Button btnAdd;
	private Context context;
	//private List<Map<String,?>> listMap;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_alarms);
        prefs = getSharedPreferences("AUTOVOLUME", MODE_PRIVATE);
        prefsEditor = prefs.edit();
        btnAdd = (Button)findViewById(R.id.btn_add_new);
        context = getApplicationContext();
        //listMap = new ArrayList<Map<String,?>>();
        
        btnAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, EditCreateAlarm.class);
				startActivityForResult(intent, 1);
			}
		});
    }

    @Override
	protected void onResume() {
		super.onResume();
		
		List<Map<String,?>> listMapLocal = new ArrayList<Map<String,?>>();
		
		Map<String,?> allPrefs = prefs.getAll();
		for (String key : allPrefs.keySet()) {
			
			Map<String,String> tmp = new HashMap<String, String>();
			tmp.put("TIME", key);
			tmp.put("VOLUME", (String)allPrefs.get(key));
			listMapLocal.add(tmp);
			
			
		}
		
        SimpleAdapter sa =  new SimpleAdapter(context, listMapLocal, R.layout.activity_list_alarm_item, new String[] { "TIME", "VOLUME" }, new int[] { R.id.tv_time, R.id.tv_volume })
        {
        	@Override
			public void setViewText(TextView v, String text) {
				super.setViewText(v, text);
				if (v.getId() == R.id.tv_time)
				{
					String[] hourMinute = text.split(":");
					int hour = Integer.valueOf(hourMinute[0]);
					int minute = Integer.valueOf(hourMinute[1]);
					
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, hour);
					c.set(Calendar.MINUTE, minute);
					
					v.setText(DateUtils.formatDateTime(context, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
				}
				else if (v.getId() == R.id.tv_volume)
				{
					v.setText(text);
				}
			}
        };
        setListAdapter(sa);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK)
		{
			String hour = String.valueOf(data.getIntExtra("HOUR", 0));
			String minute = String.valueOf(data.getIntExtra("MINUTE", 0));
			String volume = String.valueOf(data.getIntExtra("VOLUME", 0));
			
			Map<String,String> newAlarm = new HashMap<String, String>();
			newAlarm.put("TIME", hour + ":" + minute);
			newAlarm.put("VOLUME", volume);
			//listMap.add(newAlarm);
			
			prefsEditor.putString(hour + ":" + minute, volume);
			prefsEditor.commit();
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		SimpleAdapter sa = (SimpleAdapter) l.getAdapter();
		Map<String,String> item = (Map<String, String>) sa.getItem(position);
		
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list_alarms, menu);
        return true;
    }

    
}

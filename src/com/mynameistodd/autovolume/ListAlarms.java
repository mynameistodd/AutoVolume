package com.mynameistodd.autovolume;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;

public class ListAlarms extends ListActivity {

	private SharedPreferences prefs;
	private Button btnAdd;
	private Context context;
	private List<Map<String,?>> listMap;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_alarms);
        prefs = getSharedPreferences("AUTOVOLUME", MODE_PRIVATE);
        btnAdd = (Button)findViewById(R.id.btn_add_new);
        context = getApplicationContext();
        listMap = new ArrayList<Map<String,?>>();
        
        btnAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, MainActivity.class);
				startActivityForResult(intent, 1);
			}
		});
    }

    @Override
	protected void onResume() {
		super.onResume();
		
        SimpleAdapter sa =  new SimpleAdapter(context, listMap, R.layout.activity_list_alarm_item, new String[] { "TIME", "VOLUME" }, new int[] { R.id.tv_time, R.id.tv_volume })
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
			Map<String,String> newAlarm = new HashMap<String, String>();
			newAlarm.put("TIME", String.valueOf(data.getIntExtra("HOUR", 0)) + ":" + String.valueOf(data.getIntExtra("MINUTE", 0)));
			newAlarm.put("VOLUME", String.valueOf(data.getIntExtra("VOLUME", 0)));
			listMap.add(newAlarm);
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list_alarms, menu);
        return true;
    }

    
}

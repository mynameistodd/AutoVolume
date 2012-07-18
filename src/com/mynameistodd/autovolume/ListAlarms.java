package com.mynameistodd.autovolume;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v4.app.NavUtils;

public class ListAlarms extends ListActivity {

	private SharedPreferences prefs;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_alarms);
        
        Map<String,?> prefsMap = prefs.getAll();
        List<Map<String,?>> prefsList = new ArrayList<Map<String,?>>();
        prefsList.add(prefsMap);
        
        String[] keys = (String[]) prefsMap.keySet().toArray();
        int[] to = new int[] { R.id.textView1, R.id.textView2 };
        SimpleAdapter sa =  new SimpleAdapter(this, prefsList, R.layout.activity_list_alarm_item, keys, to);
        
        setListAdapter(sa);
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

package com.mynameistodd.autovolume;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ListAlarmItem extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_alarm_item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list_alarm_item, menu);
        return true;
    }

    
}

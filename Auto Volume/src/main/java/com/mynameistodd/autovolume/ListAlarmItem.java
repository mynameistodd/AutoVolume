package com.mynameistodd.autovolume;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.google.analytics.tracking.android.EasyTracker;

public class ListAlarmItem extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_alarm_item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list_alarm_item, menu);
        return true;
    }

    
}

package com.mynameistodd.autovolume;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;

public class MainActivity extends Activity implements
        EditCreateAlarm.EditCreateAlarmCallbacks,
        AlarmListFragment.AlarmListCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new AlarmListFragment())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsActivity())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        else if (id == R.id.action_add) {
            EditCreateAlarm editCreateAlarm = new EditCreateAlarm();
            Bundle args = new Bundle();
            args.putInt("ID", 0);
            editCreateAlarm.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, editCreateAlarm, "editCreate")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAlarmDismiss() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onAlarmSelected(int id) {
        EditCreateAlarm editCreateAlarm = new EditCreateAlarm();
        Bundle args = new Bundle();
        args.putInt("ID", id);
        editCreateAlarm.setArguments(args);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, editCreateAlarm, "editCreate")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}

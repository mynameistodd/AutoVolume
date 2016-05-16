package com.mynameistodd.autovolume;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivityNew extends AppCompatActivity implements AlarmRecyclerAdapter.IAdapterClicks {

    private List<Alarm> alarms;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();
        alarms = new ArrayList<>();
        alarms.addAll(MySQLiteOpenHelper.getAllAlarms(this));
        alarms.addAll(CalendarHelper.getAllAlarms(this));

        mAdapter = new AlarmRecyclerAdapter(this, alarms, this);
        mRecyclerView.setAdapter(mAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Alarm newAlarm = new Alarm(getApplicationContext());
                newAlarm.save();

                alarms.add(newAlarm);
                mAdapter.notifyItemInserted(alarms.size());
            }
        });
    }

    @Override
    public void onAlarmDelete(Alarm alarm) {
        int position = alarms.indexOf(alarm);
        alarm.delete();

        if (position > -1) {
            alarms.remove(alarm);
            mAdapter.notifyItemRemoved(position);
        }
    }
}

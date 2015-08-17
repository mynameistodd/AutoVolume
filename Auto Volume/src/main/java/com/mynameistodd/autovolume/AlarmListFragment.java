package com.mynameistodd.autovolume;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by todd on 1/11/14.
 */
public class AlarmListFragment extends Fragment implements AlarmRecyclerAdapter.IAdapterClicks {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    private List<Alarm> alarms;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    public AlarmListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        alarms = new ArrayList<>();
        alarms.addAll(MySQLiteOpenHelper.getAllAlarms(getActivity()));
        alarms.addAll(CalendarHelper.getAllAlarms(getActivity()));

        mAdapter = new AlarmRecyclerAdapter(getActivity(), alarms, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.alarm_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {

            Alarm newAlarm = new Alarm(getActivity());
            newAlarm.save();

            alarms.add(newAlarm);
            mAdapter.notifyItemInserted(alarms.size());
            return true;
        }
        return super.onOptionsItemSelected(item);
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

package com.mynameistodd.autovolume;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Created by todd on 1/11/14.
 */
public class AlarmListFragment extends ListFragment {

    public interface AlarmListCallbacks {
        public void onAlarmSelected(int id);
    }

    public AlarmListFragment() {
    }

    private ArrayAdapter<Alarm> adapter;
    private List<Alarm> alarms;
    AlarmListCallbacks mCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        alarms = MySQLiteOpenHelper.getAllAlarms(getActivity());

        adapter = new AlarmAdapter(getActivity(), android.R.layout.list_content, alarms);
        setListAdapter(adapter);
        setEmptyText(getActivity().getString(R.string.no_alarms));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Alarm item = (Alarm) getListView().getItemAtPosition(position);
        mCallbacks.onAlarmSelected(item.getId());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (AlarmListCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AlarmListCallbacks");
        }
    }
}


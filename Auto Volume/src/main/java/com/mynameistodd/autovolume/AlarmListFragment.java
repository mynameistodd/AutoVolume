package com.mynameistodd.autovolume;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import java.util.ArrayList;
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
    public void onStart() {
        super.onStart();
        Tracker easyTracker = EasyTracker.getInstance(getActivity());
        easyTracker.set(Fields.SCREEN_NAME, "AlarmListFragment");
        easyTracker.send(MapBuilder.createAppView().build());
    }

    @Override
    public void onResume() {
        super.onResume();
        alarms = MySQLiteOpenHelper.getAllAlarms(getActivity());

        adapter = new AlarmAdapter(getActivity(), android.R.layout.list_content, alarms);
        setListAdapter(adapter);
        setEmptyText(getActivity().getString(R.string.no_alarms));

        ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            List<Alarm> selAlarms = new ArrayList<Alarm>();

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                Log.d(Util.MYNAMEISTODD, "Position: " + position);
                Log.d(Util.MYNAMEISTODD, "ID: " + id);

                Alarm alarm = (Alarm) getListView().getItemAtPosition(position);
                if (checked) {
                    if (!selAlarms.contains(alarm)) {
                        selAlarms.add(alarm);
                    }
                } else {
                    if (selAlarms.contains(alarm)) {
                        selAlarms.remove(alarm);
                    }
                }

                mode.setTitle(selAlarms.size() + " selected");
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.activity_edit_create_alarm, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        deleteSelected(selAlarms);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.notifyDataSetChanged();
            }
        });


    }

    private void deleteSelected(List<Alarm> selAlarms) {
        for (Alarm alarm : selAlarms) {
            alarms.remove(alarm);
            alarm.delete();
        }
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

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(getActivity()).activityStop(getActivity());
    }
}

package com.mynameistodd.autovolume;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

    private AlarmListCallbacks mCallbacks;
    private List<Alarm> alarms;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    public AlarmListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

//        ListView listView = getListView();
//        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
//            List<Alarm> selAlarms = new ArrayList<Alarm>();
//
//            @Override
//            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
//                Log.d(Util.MYNAMEISTODD, "Position: " + position);
//                Log.d(Util.MYNAMEISTODD, "ID: " + id);
//
//                Alarm alarm = (Alarm) getListView().getItemAtPosition(position);
//                if (checked) {
//                    if (!selAlarms.contains(alarm)) {
//                        selAlarms.add(alarm);
//                    }
//                } else {
//                    if (selAlarms.contains(alarm)) {
//                        selAlarms.remove(alarm);
//                    }
//                }
//
//                mode.setTitle(selAlarms.size() + " selected");
//            }
//
//            @Override
//            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                MenuInflater inflater = mode.getMenuInflater();
//                inflater.inflate(R.menu.activity_edit_create_alarm, menu);
//                return true;
//            }
//
//            @Override
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//
//            @Override
//            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.menu_delete:
//                        deleteSelected(selAlarms);
//                        mode.finish();
//                        return true;
//                    default:
//                        return false;
//                }
//            }
//
//            @Override
//            public void onDestroyActionMode(ActionMode mode) {
//                adapter.notifyDataSetChanged();
//            }
//        });
    }

//    private void deleteSelected(List<Alarm> selAlarms) {
//        for (Alarm alarm : selAlarms) {
//            alarms.remove(alarm);
//            alarm.delete();
//        }
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (AlarmListCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AlarmListCallbacks");
        }

        analytics = GoogleAnalytics.getInstance(getActivity());
        tracker = analytics.newTracker(R.xml.global_tracker);
    }

    @Override
    public void onItemClick(int position) {
        Alarm item = alarms.get(position);
        mCallbacks.onAlarmSelected(item.getId());
    }

    public interface AlarmListCallbacks {
        void onAlarmSelected(int id);
    }
}

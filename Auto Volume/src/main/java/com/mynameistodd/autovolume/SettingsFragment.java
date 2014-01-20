package com.mynameistodd.autovolume;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Tracker easyTracker = EasyTracker.getInstance(getActivity());
        easyTracker.set(Fields.SCREEN_NAME, "SettingsFragment");
        easyTracker.send(MapBuilder.createAppView().build());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(getActivity()).activityStop(getActivity());
    }
}

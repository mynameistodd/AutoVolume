package com.mynameistodd.autovolume;

import android.os.Bundle;
import android.preference.PreferenceFragment;

//import com.google.analytics.tracking.android.EasyTracker;

public class SettingsActivity extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onStart() {
        super.onStart();
        //EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //EasyTracker.getInstance(this).activityStop(this);
    }
}

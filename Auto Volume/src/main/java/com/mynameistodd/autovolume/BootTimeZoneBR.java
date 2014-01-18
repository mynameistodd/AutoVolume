package com.mynameistodd.autovolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class BootTimeZoneBR extends BroadcastReceiver {

    private List<Alarm> allAlarms;

    public BootTimeZoneBR() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Util.MYNAMEISTODD, "Setting alarms...");

        allAlarms = MySQLiteOpenHelper.getAllAlarms(context);

        for (Alarm alarm : allAlarms) {
            if (intent.getAction() == Intent.ACTION_TIMEZONE_CHANGED) {
                //Cancel the alarms already scheduled if changing time zones.
                alarm.cancel();
            }
            alarm.schedule();
        }
    }

}

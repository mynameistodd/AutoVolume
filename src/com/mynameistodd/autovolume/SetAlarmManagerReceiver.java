package com.mynameistodd.autovolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

public class SetAlarmManagerReceiver extends BroadcastReceiver {

	private AudioManager audioManager;
	private int audioLevel;
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		audioManager = (AudioManager) arg0.getSystemService(Context.AUDIO_SERVICE);
		audioLevel = arg1.getIntExtra("AUDIO_LEVEL", 0);
		Log.d("MYNAMEISTODD", "Data from Intent:" + arg1.getData());
		
		audioManager.setStreamVolume(AudioManager.STREAM_RING, audioLevel, AudioManager.FLAG_SHOW_UI);
		Toast.makeText(arg0, "Volume Changed!", Toast.LENGTH_SHORT).show();
		Log.d("MYNAMEISTODD", "Volume set to:" + audioLevel);
	}

}

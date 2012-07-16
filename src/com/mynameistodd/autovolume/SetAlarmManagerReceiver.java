package com.mynameistodd.autovolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class SetAlarmManagerReceiver extends BroadcastReceiver {

	private AudioManager audioManager;
	private int audioLevel;
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		audioManager = (AudioManager) arg0.getSystemService(Context.AUDIO_SERVICE);
		audioLevel = arg1.getIntExtra("AUDIO_LEVEL", 0);
		
		audioManager.setStreamVolume(AudioManager.STREAM_RING, audioLevel, AudioManager.FLAG_SHOW_UI);
	}

}

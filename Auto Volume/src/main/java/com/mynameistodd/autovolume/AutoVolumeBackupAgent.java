package com.mynameistodd.autovolume;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class AutoVolumeBackupAgent extends BackupAgentHelper {

	static final String PREFS_BACKUP_KEY = "prefs";
	
	@Override
	public void onCreate() {
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, Util.AUTOVOLUME);
		addHelper(PREFS_BACKUP_KEY, helper);
	}

}

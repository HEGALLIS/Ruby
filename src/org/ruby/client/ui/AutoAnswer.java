package org.ruby.client.ui;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class AutoAnswer extends Activity {
	AudioManager am;

	boolean getMode() {
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Settings.PREF_AUTO_DEMAND, Settings.DEFAULT_AUTO_DEMAND);
	}
	
	void restoreVolume() {
		am.setStreamVolume(AudioManager.STREAM_RING,1,0);
		am.setRingerMode(
				PreferenceManager.getDefaultSharedPreferences(this).getInt("ringermode"+getMode(), 
				AudioManager.RINGER_MODE_NORMAL));
		am.setStreamVolume(AudioManager.STREAM_RING,
				PreferenceManager.getDefaultSharedPreferences(this).getInt("volume"+getMode(), 
				am.getStreamMaxVolume(AudioManager.STREAM_RING)*
				(getMode()?4:3)/4
				),AudioManager.FLAG_VIBRATE | AudioManager.FLAG_SHOW_UI);
	}
	
	void saveVolume() {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
		
		edit.putInt("volume"+getMode(),am.getStreamVolume(AudioManager.STREAM_RING));
		edit.putInt("ringermode"+getMode(),am.getRingerMode());
		edit.commit();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
		
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		saveVolume();
		edit.putBoolean(Settings.PREF_AUTO_DEMAND, !getMode());
		edit.commit();
		restoreVolume();
		Receiver.updateAutoAnswer();
		finish();
	}
}

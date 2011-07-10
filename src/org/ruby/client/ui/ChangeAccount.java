package org.ruby.client.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class ChangeAccount extends Activity {

	public static int getPref(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(Settings.PREF_ACCOUNT, Settings.DEFAULT_ACCOUNT);
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
		
		edit.putInt(Settings.PREF_ACCOUNT, Receiver.engine(this).pref = 1-getPref(this));
		edit.commit();
		Receiver.engine(this).register();
		finish();
	}
}

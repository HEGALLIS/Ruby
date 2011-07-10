package org.ruby.client.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class SIP extends Activity {

	void callPSTN(String uri) {
		String number;
		
		if (uri.indexOf(":") >= 0) {
			number = uri.substring(uri.indexOf(":")+1);
			if (!number.equals("")) {
		        Intent intent = new Intent(Intent.ACTION_CALL,
		                Uri.fromParts(Uri.decode(number).contains("@")?"ruby":"tel", Uri.decode(number)+
		                		(PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.PREF_PREF, Settings.DEFAULT_PREF).equals(Settings.VAL_PREF_PSTN) ? "+" : ""), null));
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        Caller.noexclude = SystemClock.elapsedRealtime();
		        startActivity(intent);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		Intent intent;
		Uri uri;
		Ruby.on(this,true);
		if ((intent = getIntent()) != null
			&& (uri = intent.getData()) != null)
				callPSTN(uri.toString());
		finish();
	}
}

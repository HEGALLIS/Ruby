package org.ruby.client.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class PSTN extends Activity {

	static void callPSTN(String uri) {
		String number;
		
		if (uri.indexOf(":") >= 0) {
			number = uri.substring(uri.indexOf(":")+1);
			if (!number.equals("")) {
		        Intent intent = new Intent(Intent.ACTION_CALL,
		                Uri.fromParts("tel", Uri.decode(number)+
		                		(!PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(Settings.PREF_PREF, Settings.DEFAULT_PREF).equals(Settings.VAL_PREF_PSTN) ? "+" : ""), null));
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        Receiver.mContext.startActivity(intent);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		Intent intent;
		Uri uri;
    	if (Receiver.mContext == null) Receiver.mContext = this;
		if ((intent = getIntent()) != null
			&& (uri = intent.getData()) != null)
				callPSTN(uri.toString());
		finish();
	}
}

package org.ruby.client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class OneShotAlarm2 extends BroadcastReceiver {

    @Override
	public void onReceive(Context context, Intent intent) {
    	if (!Ruby.release) Log.i("SipUA:","alarm2");
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_WLAN, Settings.DEFAULT_WLAN) ||
        		PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_3G, Settings.DEFAULT_3G) ||
        		PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_VPN, Settings.DEFAULT_VPN) ||
        		PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_EDGE, Settings.DEFAULT_EDGE)) {
        	context.startService(new Intent(context,RegisterService.class));
        } else
        	context.stopService(new Intent(context,RegisterService.class));
    }
}

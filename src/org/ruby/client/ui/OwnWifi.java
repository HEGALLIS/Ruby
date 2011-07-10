package org.ruby.client.ui;

import org.ruby.client.UserAgent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OwnWifi extends BroadcastReceiver {

    @Override
	public void onReceive(Context context, Intent intent) {
    	if (!Ruby.release) Log.i("SipUA:","ownwifi");
    	if (Receiver.mContext == null) Receiver.mContext = context;
    	if (Receiver.call_state == UserAgent.UA_STATE_IDLE)
    		Receiver.enable_wifi(false);
    }
}

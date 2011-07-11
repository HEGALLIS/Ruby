package org.ruby.client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LoopAlarm extends BroadcastReceiver {

    @Override
	public void onReceive(Context context, Intent intent) {
    	if (!Ruby.release) Log.i("Client:","alarm");
    	Receiver.engine(context).keepAlive();
    }
}

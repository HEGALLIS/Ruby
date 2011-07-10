package org.ruby.client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LoopLocation extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		OneShotLocation.receive(context,intent);
    }
}

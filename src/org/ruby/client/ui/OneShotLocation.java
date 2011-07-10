package org.ruby.client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class OneShotLocation extends BroadcastReceiver {

	public static void receive(Context context, Intent intent) {
		Location loc;

		if (!Ruby.release) Log.i("SipUA:",intent.getExtras().keySet().toString());
    	if (Receiver.mContext == null) Receiver.mContext = context;
    	loc = (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
    	if (loc != null) {
    		Receiver.pos(false);
    		Receiver.url("lat="+loc.getLatitude()+"&lon="+loc.getLongitude()+"&rad="+loc.getAccuracy());
    	} else if (intent.hasExtra(Intent.EXTRA_ALARM_COUNT))
    		Receiver.pos(false);		
	}
	@Override
	
	public void onReceive(Context context, Intent intent) {
		receive(context, intent);
    }
}

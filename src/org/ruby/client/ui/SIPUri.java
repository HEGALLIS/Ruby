package org.ruby.client.ui;

import org.ruby.client.R;
import org.ruby.client.RubyEngine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;

public class SIPUri extends Activity {

	void call(String target) {
		if (!Receiver.engine(this).call(target,true)) {
			new AlertDialog.Builder(this)
			.setMessage(R.string.notfast)
			.setTitle(R.string.app_name)
			.setIcon(R.drawable.icon22)
			.setCancelable(true)
			.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			})
			.show();
		} else
			finish();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	if (Receiver.mContext == null) Receiver.mContext = this;

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Ruby.on(this,true);
		Uri uri = getIntent().getData();
		String target;
		if (uri.getScheme().equals("sip") || uri.getScheme().equals("ruby"))
			target = uri.getSchemeSpecificPart();
		else {
			if (uri.getAuthority().equals("aim") ||
					uri.getAuthority().equals("yahoo") ||
					uri.getAuthority().equals("icq") ||
					uri.getAuthority().equals("gtalk") ||
					uri.getAuthority().equals("msn"))
				target = uri.getLastPathSegment().replaceAll("@","_at_") + "@" + uri.getAuthority() + ".gtalk2voip.com";
			else if (uri.getAuthority().equals("skype"))
				target = uri.getLastPathSegment() + "@" + uri.getAuthority();
			else
				target = uri.getLastPathSegment();
		}
		if (!Ruby.release) Log.v("SIPUri", "sip uri: " + target);
		if (!target.contains("@") && PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.PREF_PREF, Settings.DEFAULT_PREF).equals(Settings.VAL_PREF_ASK)) {
			final String t = target;
			String items[] = {getString(R.string.pstn_name)};
			for (int p = 0; p < RubyEngine.LINES; p++)
				if (Receiver.isFast(p) || (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Settings.PREF_CALLBACK, Settings.DEFAULT_CALLBACK) &&
						PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.PREF_POSURL, Settings.DEFAULT_POSURL).length() > 0)) {
					items = new String[2];
					items[0] = getString(R.string.app_name);
					items[1] = getString(R.string.pstn_name);
					break;
				}
			final String fitems[] = items;
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon22)
			.setTitle(target)
            .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	if (fitems[whichButton].equals(getString(R.string.app_name)))
                    		call(t);
                    	else {
                			PSTN.callPSTN("sip:"+t);
                			finish();
                    	}
                    }
                })
			.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			})
			.show();
		} else
			call(target); 
	}
	
	    @Override
	    public void onPause() {
	        super.onPause();
	        finish();
	    }
	 
}

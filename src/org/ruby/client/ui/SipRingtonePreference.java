package org.ruby.client.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;

public class SipRingtonePreference extends RingtonePreference 
{   
	private Context mContext;
	
    public SipRingtonePreference(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
    	mContext = context;
    }

    @Override
    protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) 
    {    	
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        ringtonePickerIntent.putExtras(new Intent( RingtoneManager.ACTION_RINGTONE_PICKER));
    }

    @Override
    protected void onSaveRingtone(Uri ringtoneUri) 
    {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		edit.putString(org.ruby.client.ui.Settings.PREF_SIPRINGTONE, ringtoneUri != null ? ringtoneUri.toString() : org.ruby.client.ui.Settings.DEFAULT_SIPRINGTONE);		
		edit.commit();        
    }

    @Override
    protected Uri onRestoreRingtone() 
    {
        String uriString = PreferenceManager.getDefaultSharedPreferences(mContext).getString(org.ruby.client.ui.Settings.PREF_SIPRINGTONE,
        		Settings.System.DEFAULT_RINGTONE_URI.toString());
        return !TextUtils.isEmpty(uriString) ? Uri.parse(uriString) : null;        
    }    
}

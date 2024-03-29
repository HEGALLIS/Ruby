package org.ruby.codecs;

import org.ruby.client.ui.Receiver;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

class CodecBase implements Preference.OnPreferenceChangeListener {
	protected String CODEC_NAME;
	protected String CODEC_USER_NAME;
	protected int CODEC_NUMBER;
	protected int CODEC_SAMPLE_RATE=8000;		// default for most narrow band codecs
	protected int CODEC_FRAME_SIZE=160;		// default for most narrow band codecs
	protected String CODEC_DESCRIPTION;
	protected String CODEC_DEFAULT_SETTING = "never";

	private boolean loaded = false,failed = false;
	private boolean enabled = false;
	private boolean wlanOnly = false,wlanOr3GOnly = false;
	private String value;

	public void update() {
		if (Receiver.mContext != null) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
			value = sp.getString(key(), CODEC_DEFAULT_SETTING);
			updateFlags(value);		
		}
	}
	
	public String getValue() {
		return value;
	}
	
	void load() {
		update();
		loaded = true;
	}
	
	public int samp_rate() {
		return CODEC_SAMPLE_RATE;
	}
	
	public int frame_size() {
		return CODEC_FRAME_SIZE;
	}

	public boolean isLoaded() {
		return loaded;
	}
    
	public boolean isFailed() {
		return failed;
	}
	
	public void fail() {
		update();
		failed = true;
	}
	
	public void enable(boolean e) {
		enabled = e;
	}

	public boolean isEnabled() {
		return enabled;
	}

	TelephonyManager tm;
	int nt;
	
	public boolean isValid() {
		if (!isEnabled())
			return false;
		if (Receiver.on_wlan)
			return true;
		if (wlanOnly())
			return false;
		if (tm == null) tm = (TelephonyManager) Receiver.mContext.getSystemService(Context.TELEPHONY_SERVICE);
		nt = tm.getNetworkType();
		if (wlanOr3GOnly() && nt < TelephonyManager.NETWORK_TYPE_UMTS)
			return false;
		if (nt < TelephonyManager.NETWORK_TYPE_EDGE)
			return false;
		return true;
	}
		
	private boolean wlanOnly() {
		return enabled && wlanOnly;
	}
	
	private boolean wlanOr3GOnly() {
		return enabled && wlanOr3GOnly;
	}

	public String name() {
		return CODEC_NAME;
	}

	public String key() {
		return CODEC_NAME+"_new";
	}
	
	public String userName() {
		return CODEC_USER_NAME;
	}

	public String getTitle() {
		return CODEC_NAME + " (" + CODEC_DESCRIPTION + ")";
	}

	public int number() {
		return CODEC_NUMBER;
	}

	public void setListPreference(ListPreference l) {
		l.setOnPreferenceChangeListener(this);
		l.setValue(value);
	}

	public boolean onPreferenceChange(Preference p, Object newValue) {
		ListPreference l = (ListPreference)p;
		value = (String)newValue;

		updateFlags(value);

		l.setValue(value);
		l.setSummary(l.getEntry());

		return true;
	}

	private void updateFlags(String v) {

		if (v.equals("never")) {
			enabled = false;
		} else {
			enabled = true;
			if (v.equals("wlan"))
				wlanOnly = true;
			else
				wlanOnly = false;
			if (v.equals("wlanor3g"))
				wlanOr3GOnly = true;
			else
				wlanOr3GOnly = false;
		}
	}

	public String toString() {
		return "CODEC{ " + CODEC_NUMBER + ": " + getTitle() + "}";
	}
}

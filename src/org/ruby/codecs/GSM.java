package org.ruby.codecs;

import org.ruby.client.ui.Receiver;
import org.ruby.client.ui.Settings;
import org.ruby.client.ui.Ruby;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class GSM extends CodecBase implements Codec {
	GSM() {
		CODEC_NAME = "GSM";
		CODEC_USER_NAME = "GSM";
		CODEC_DESCRIPTION = "13kbit";
		CODEC_NUMBER = 3;
		CODEC_DEFAULT_SETTING = "always";
		/* up convert original compression parameter for this codec */
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
		String pref = sp.getString(Settings.PREF_COMPRESSION, Settings.DEFAULT_COMPRESSION);
		if (pref != null) {
			SharedPreferences.Editor e = sp.edit();
			e.remove("compression");
			e.putString(CODEC_NAME, pref);
			e.commit();
		}
		super.update();
	}

	void load() {
		try {
			System.loadLibrary("gsm_jni");
			super.load();
		} catch (Throwable e) {
			if (!Ruby.release) e.printStackTrace();
		}
    
	}  
 	
	public native int open();
	public native int decode(byte encoded[], short lin[], int size);
	public native int encode(short lin[], int offset, byte encoded[], int size);
	public native void close();
	
	public void init() {
		load();
		if (isLoaded())
			open();
	}

}

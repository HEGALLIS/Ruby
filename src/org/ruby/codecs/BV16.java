package org.ruby.codecs;

import org.ruby.client.ui.Ruby;

class BV16 extends CodecBase implements Codec {

// 	private static final int DEFAULT_COMPRESSION = 6;

	BV16() {
		CODEC_NAME = "BV16";
		CODEC_USER_NAME = "BV16";
		CODEC_DESCRIPTION = "16kbit";
		CODEC_NUMBER = 106;
		CODEC_DEFAULT_SETTING = "always";
		super.update();
	}


	void load() {
		try {
			System.loadLibrary("bv16_jni");
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

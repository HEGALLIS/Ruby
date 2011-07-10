package org.ruby.codecs;

import org.ruby.client.ui.Ruby;

class Speex extends CodecBase implements Codec {

	/* quality
	 * 1 : 4kbps (very noticeable artifacts, usually intelligible)
	 * 2 : 6kbps (very noticeable artifacts, good intelligibility)
	 * 4 : 8kbps (noticeable artifacts sometimes)
	 * 6 : 11kpbs (artifacts usually only noticeable with headphones)
	 * 8 : 15kbps (artifacts not usually noticeable)
	 */
	private static final int DEFAULT_COMPRESSION = 6;

	Speex() {
		CODEC_NAME = "speex";
		CODEC_USER_NAME = "speex";
		CODEC_DESCRIPTION = "11kbit";
		CODEC_NUMBER = 97;
		CODEC_DEFAULT_SETTING = "always";
		super.update();
	}

	void load() {
		try {
			System.loadLibrary("speex_jni");
			super.load();
		} catch (Throwable e) {
			if (!Ruby.release) e.printStackTrace();
		}

	}

	public native int open(int compression);
	public native int decode(byte encoded[], short lin[], int size);
	public native int encode(short lin[], int offset, byte encoded[], int size);
	public native void close();

	public void init() {
		load();
		if (isLoaded())
			open(DEFAULT_COMPRESSION);
	}
}

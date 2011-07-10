package org.ruby.codecs;

import org.ruby.client.ui.Ruby;

class SILK24 extends CodecBase implements Codec { 
	/* 
	 *                 | fs (Hz) | BR (kbps)
	 * ----------------+---------+---------
	 * Narrowband	   | 8000    | 6 -20
	 * Mediumband      | 12000   | 7 -25
	 * Wideband        | 16000   | 8 -30
	 * Super Wideband  | 24000   | 12 -40
	 *
	 * Table 1: fs specifies the audio sampling frequency in Hertz (Hz); BR
	 * specifies the adaptive bit rate range in kilobits per second (kbps).
	 * 
	 * Complexity can be scaled to optimize for CPU resources in real-time,
	 * mostly in trade-off to network bit rate. 0 is least CPU demanding and
	 * highest bit rate. 
	 */
	private static final int DEFAULT_COMPLEXITY = 0;
	
	SILK24() {
		CODEC_USER_NAME = "SILK"; 
		CODEC_NAME = "silk24"; 
		CODEC_DESCRIPTION = "12-40kbit"; 
		CODEC_NUMBER = 120;
		CODEC_DEFAULT_SETTING = "never";
		CODEC_SAMPLE_RATE = 24000;
		CODEC_FRAME_SIZE = 480;
		super.update();
	}


	void load() {
		try {
//			System.loadLibrary("silkcommon");
			System.loadLibrary("silk24_jni");
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
			open(DEFAULT_COMPLEXITY);
	}
}

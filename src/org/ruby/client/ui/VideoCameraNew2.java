package org.ruby.client.ui;

import java.io.IOException;

import android.hardware.Camera;

public class VideoCameraNew2 {
	static void reconnect(Camera c) {
		try {
			c.reconnect();
		} catch (IOException e) {
			if (!Ruby.release) e.printStackTrace();
		}
	}
}



package org.ruby.client.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Activity2 extends Activity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Intent startActivity = new Intent();
    	startActivity.setClass(this,InCallScreen.class);
	    startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(startActivity); 
    	finish();
	}
}

package org.ruby.client.phone;

import android.content.Context;
import android.telephony.ServiceState;

public interface Phone {
	enum State { IDLE,RINGING,OFFHOOK };
	
	State getState();
	
	enum SuppService { UNKNOWN, SWITCH, SEPARATE, TRANSFER, CONFERENCE, REJECT, HANGUP };
	Context getContext();
	Call getForegroundCall();
	Call getBackgroundCall();
	Call getRingingCall();
	
	ServiceState getServiceState();
}

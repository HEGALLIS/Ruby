package org.ruby.client;

import java.io.IOException;
import java.net.UnknownHostException;

import org.ruby.net.KeepAliveSip;
import org.ruby.client.ui.ChangeAccount;
import org.ruby.client.ui.LoopAlarm;
import org.ruby.client.ui.Receiver;
import org.ruby.client.ui.Settings;
import org.ruby.client.ui.Ruby;
import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class RubyEngine implements RegisterAgentListener {

	public static final int LINES = 2;
	public int pref;
	
	public static final int UNINITIALIZED = 0x0;
	public static final int INITIALIZED = 0x2;
	
	/** User Agent */
	public UserAgent[] uas;
	public UserAgent ua;

	/** Register Agent */
	private RegisterAgent[] ras;

	private KeepAliveSip[] kas;
	
	/** UserAgentProfile */
	public UserAgentProfile[] user_profiles;

	public SipProvider[] sip_providers;
	
	public static PowerManager.WakeLock[] wl,pwl;
	
	UserAgentProfile getUserAgentProfile(String suffix) {
		UserAgentProfile user_profile = new UserAgentProfile(null);
		
		user_profile.username = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_USERNAME+suffix, Settings.DEFAULT_USERNAME); // modified
		user_profile.passwd = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_PASSWORD+suffix, Settings.DEFAULT_PASSWORD);
		if (PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_DOMAIN+suffix, Settings.DEFAULT_DOMAIN).length() == 0) {
			user_profile.realm = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_SERVER+suffix, Settings.DEFAULT_SERVER);
		} else {
			user_profile.realm = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_DOMAIN+suffix, Settings.DEFAULT_DOMAIN);
		}
		user_profile.realm_orig = user_profile.realm;
		if (PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_FROMUSER+suffix, Settings.DEFAULT_FROMUSER).length() == 0) {
			user_profile.from_url = user_profile.username;
		} else {
			user_profile.from_url = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_FROMUSER+suffix, Settings.DEFAULT_FROMUSER);
		}
		
		// MMTel configuration (added by mandrajg)
		user_profile.qvalue = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_MMTEL_QVALUE, Settings.DEFAULT_MMTEL_QVALUE);
		user_profile.mmtel = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_MMTEL, Settings.DEFAULT_MMTEL);

		user_profile.pub = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_EDGE+suffix, Settings.DEFAULT_EDGE) ||
			PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_3G+suffix, Settings.DEFAULT_3G);
		return user_profile;
	}

	public boolean StartEngine() {
			PowerManager pm = (PowerManager) getUIContext().getSystemService(Context.POWER_SERVICE);
			if (wl == null) {
				if (!PreferenceManager.getDefaultSharedPreferences(getUIContext()).contains(org.ruby.client.ui.Settings.PREF_KEEPON)) {
					Editor edit = PreferenceManager.getDefaultSharedPreferences(getUIContext()).edit();
	
					edit.putBoolean(org.ruby.client.ui.Settings.PREF_KEEPON, Build.MODEL.equals("Nexus One") ||
							Build.MODEL.equals("Nexus S") ||
							Build.MODEL.equals("Archos5") ||
							Build.MODEL.equals("ADR6300") ||
							Build.MODEL.equals("PC36100") ||
							Build.MODEL.equals("HTC Desire") ||
							Build.MODEL.equals("HTC Incredible S") ||
							Build.MODEL.equals("HTC Wildfire"));
					edit.commit();
				}
				wl = new PowerManager.WakeLock[LINES];
				pwl = new PowerManager.WakeLock[LINES];
			}
			pref = ChangeAccount.getPref(Receiver.mContext);

			uas = new UserAgent[LINES];
			ras = new RegisterAgent[LINES];
			kas = new KeepAliveSip[LINES];
			lastmsgs = new String[LINES];
			sip_providers = new SipProvider[LINES];
			user_profiles = new UserAgentProfile[LINES];
			user_profiles[0] = getUserAgentProfile("");
			for (int i = 1; i < LINES; i++)
				user_profiles[1] = getUserAgentProfile(""+i);
			
			SipStack.init(null);
			int i = 0;
			for (UserAgentProfile user_profile : user_profiles) {
				if (wl[i] == null) {
					wl[i] = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Ruby.RubyEngine");
					if (PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(org.ruby.client.ui.Settings.PREF_KEEPON, org.ruby.client.ui.Settings.DEFAULT_KEEPON))
						pwl[i] = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Ruby.RubyEngine");
				}
				
				try {
					SipStack.debug_level = 0;
		//			SipStack.log_path = "/data/data/org.Rubyent";
					SipStack.max_retransmission_timeout = 4000;
					SipStack.default_transport_protocols = new String[1];
					SipStack.default_transport_protocols[0] = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_PROTOCOL+(i!=0?i:""),
							user_profile.realm.equals(Settings.DEFAULT_SERVER)?"tcp":"udp");
					
					String version = "Ruby/" + Ruby.getVersion() + "/" + Build.MODEL;
					SipStack.ua_info = version;
					SipStack.server_info = version;
						
					IpAddress.setLocalIpAddress();
					sip_providers[i] = new SipProvider(IpAddress.localIpAddress, 0);
					user_profile.contact_url = getContactURL(user_profile.username,sip_providers[i]);
					
					if (user_profile.from_url.indexOf("@") < 0) {
						user_profile.from_url +=
							"@"
							+ user_profile.realm;
					}
					
					CheckEngine();
					
					// added by mandrajg
					String icsi = null;
					if (user_profile.mmtel == true){
						icsi = "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"";
					}
		
					uas[i] = ua = new UserAgent(sip_providers[i], user_profile);
					ras[i] = new RegisterAgent(sip_providers[i], user_profile.from_url, // modified
							user_profile.contact_url, user_profile.username,
							user_profile.realm, user_profile.passwd, this, user_profile,
							user_profile.qvalue, icsi, user_profile.pub); // added by mandrajg
					kas[i] = new KeepAliveSip(sip_providers[i],100000);
				} catch (Exception E) {
				}
				i++;
			}
			register();
			listen();

			return true;
	}

	private String getContactURL(String username,SipProvider sip_provider) {
		int i = username.indexOf("@");
		if (i != -1) {
			// if the username already contains a @ 
			//strip it and everthing following it
			username = username.substring(0, i);
		}

		return username + "@" + IpAddress.localIpAddress
		+ (sip_provider.getPort() != 0?":"+sip_provider.getPort():"")
		+ ";transport=" + sip_provider.getDefaultTransport();		
	}
	
	void setOutboundProxy(SipProvider sip_provider,int i) {
		try {
			if (sip_provider != null) sip_provider.setOutboundProxy(new SocketAddress(
					IpAddress.getByName(PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_DNS+i, Settings.DEFAULT_DNS)),
					Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_PORT+(i!=0?i:""), Settings.DEFAULT_PORT))));
		} catch (Exception e) {
		}
	}
	
	public void CheckEngine() {
		int i = 0;
		for (SipProvider sip_provider : sip_providers) {
			if (sip_provider != null && !sip_provider.hasOutboundProxy())
				setOutboundProxy(sip_provider,i);
			i++;
		}
	}

	public Context getUIContext() {
		return Receiver.mContext;
	}
	
	public int getRemoteVideo() {
		return ua.remote_video_port;
	}
	
	public int getLocalVideo() {
		return ua.local_video_port;
	}
	
	public String getRemoteAddr() {
		return ua.remote_media_address;
	}
	
	public void expire() {
		Receiver.expire_time = 0;
		int i = 0;
		for (RegisterAgent ra : ras) {
			if (ra != null && ra.CurrentState == RegisterAgent.REGISTERED) {
				ra.CurrentState = RegisterAgent.UNREGISTERED;
				Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0, 0);
			}
			i++;
		}
		register();
	}
	
	public void unregister(int i) {
			if (user_profiles[i] == null || user_profiles[i].username.equals("") ||
					user_profiles[i].realm.equals("")) return;

			RegisterAgent ra = ras[i];
			if (ra != null && ra.unregister()) {
				Receiver.alarm(0, LoopAlarm.class);
				Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(R.string.reg),R.drawable.sym_presence_idle,0);
				wl[i].acquire();
			} else
				Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0, 0);
	}
	
	public void registerMore() {
		IpAddress.setLocalIpAddress();
		int i = 0;
		for (RegisterAgent ra : ras) {
			try {
				if (user_profiles[i] == null || user_profiles[i].username.equals("") ||
						user_profiles[i].realm.equals("")) {
					i++;
					continue;
				}
				user_profiles[i].contact_url = getContactURL(user_profiles[i].from_url,sip_providers[i]);
		
				if (ra != null && !ra.isRegistered() && Receiver.isFast(i) && ra.register()) {
					Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(R.string.reg),R.drawable.sym_presence_idle,0);
					wl[i].acquire();
				}
			} catch (Exception ex) {
				
			}
			i++;
		}
	}
	
	public void register() {
		IpAddress.setLocalIpAddress();
		int i = 0;
		for (RegisterAgent ra : ras) {
			try {
				if (user_profiles[i] == null || user_profiles[i].username.equals("") ||
						user_profiles[i].realm.equals("")) {
					i++;
					continue;
				}
				user_profiles[i].contact_url = getContactURL(user_profiles[i].from_url,sip_providers[i]);
		
				if (!Receiver.isFast(i)) {
					unregister(i);
				} else {
					if (ra != null && ra.register()) {
						Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(R.string.reg),R.drawable.sym_presence_idle,0);
						wl[i].acquire();
					}
				}
			} catch (Exception ex) {
				
			}
			i++;
		}
	}
	
	public void registerUdp() {
		IpAddress.setLocalIpAddress();
		int i = 0;
		for (RegisterAgent ra : ras) {
			try {
				if (user_profiles[i] == null || user_profiles[i].username.equals("") ||
						user_profiles[i].realm.equals("") ||
						sip_providers[i] == null ||
						sip_providers[i].getDefaultTransport() == null ||
						sip_providers[i].getDefaultTransport().equals("tcp")) {
					i++;
					continue;
				}
				user_profiles[i].contact_url = getContactURL(user_profiles[i].from_url,sip_providers[i]);
		
				if (!Receiver.isFast(i)) {
					unregister(i);
				} else {
					if (ra != null && ra.register()) {
						Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(R.string.reg),R.drawable.sym_presence_idle,0);
						wl[i].acquire();
					}
				}
			} catch (Exception ex) {
				
			}
			i++;
		}
	}

	public void halt() { // modified
		long time = SystemClock.elapsedRealtime();
		
		int i = 0;
		for (RegisterAgent ra : ras) {
			unregister(i);
			while (ra != null && ra.CurrentState != RegisterAgent.UNREGISTERED && SystemClock.elapsedRealtime()-time < 2000)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			if (wl[i].isHeld()) {
				wl[i].release();
				if (pwl[i] != null && pwl[i].isHeld()) pwl[i].release();
			}
			if (kas[i] != null) {
				Receiver.alarm(0, LoopAlarm.class);
				kas[i].halt();
			}
			Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0, 0);
			if (ra != null)
				ra.halt();
			if (uas[i] != null)
				uas[i].hangup();
			if (sip_providers[i] != null)
				sip_providers[i].halt();
			i++;
		}
	}

	public boolean isRegistered()
	{
		for (RegisterAgent ra : ras)
			if (ra != null && ra.isRegistered())
				return true;
		return false;
	}
	
	boolean isRegistered(int i)
	{
		if (ras[i] == null)
		{
			return false;
		}
		return ras[i].isRegistered();
	}
	
	public void onUaRegistrationSuccess(RegisterAgent reg_ra, NameAddress target,
			NameAddress contact, String result) {
    	int i = 0;
    	for (RegisterAgent ra : ras) {
    		if (ra == reg_ra) break;
    		i++;
    	}
		if (isRegistered(i)) {
			if (Receiver.on_wlan)
				Receiver.alarm(60, LoopAlarm.class);
			Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(i == pref?R.string.regpref:R.string.regclick),R.drawable.sym_presence_available,0);
			reg_ra.subattempts = 0;
			reg_ra.startMWI();
			Receiver.registered();
		} else
			Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0,0);
		if (wl[i].isHeld()) {
			wl[i].release();
			if (pwl[i] != null && pwl[i].isHeld()) pwl[i].release();
		}
	}

	String[] lastmsgs;
	
    public void onMWIUpdate(RegisterAgent mwi_ra, boolean voicemail, int number, String vmacc) {
    	int i = 0;
    	for (RegisterAgent ra : ras) {
    		if (ra == mwi_ra) break;
    		i++;
    	}
    	if (i != pref) return;
		if (voicemail) {
			String msgs = getUIContext().getString(R.string.voicemail);
			if (number != 0) {
				msgs = msgs + ": " + number;
			}
			Receiver.MWI_account = vmacc;
			if (lastmsgs[i] == null || !msgs.equals(lastmsgs[i])) {
				Receiver.onText(Receiver.MWI_NOTIFICATION, msgs,android.R.drawable.stat_notify_voicemail,0);
				lastmsgs[i] = msgs;
			}
		} else {
			Receiver.onText(Receiver.MWI_NOTIFICATION, null, 0,0);
			lastmsgs[i] = null;
		}
	}

	static long lasthalt,lastpwl;
	
	/** When a UA failed on (un)registering. */
	public void onUaRegistrationFailure(RegisterAgent reg_ra, NameAddress target,
			NameAddress contact, String result) {
		boolean retry = false;
    	int i = 0;
    	for (RegisterAgent ra : ras) {
    		if (ra == reg_ra) break;
    		i++;
    	}
    	if (isRegistered(i)) {
    		reg_ra.CurrentState = RegisterAgent.UNREGISTERED;
    		Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0, 0);
    	} else {
    		retry = true;
    		Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(R.string.regfailed)+" ("+result+")",R.drawable.sym_presence_away,0);
    	}
    	if (retry && SystemClock.uptimeMillis() > lastpwl + 45000 && pwl[i] != null && !pwl[i].isHeld() && Receiver.on_wlan) {
			lastpwl = SystemClock.uptimeMillis();
			if (wl[i].isHeld())
				wl[i].release();
			pwl[i].acquire();
			register();
			if (!wl[i].isHeld() && pwl[i].isHeld()) pwl[i].release();
		} else if (wl[i].isHeld()) {
			wl[i].release();
			if (pwl[i] != null && pwl[i].isHeld()) pwl[i].release();
		}
		if (SystemClock.uptimeMillis() > lasthalt + 45000) {
			lasthalt = SystemClock.uptimeMillis();
			sip_providers[i].haltConnections();
		}
		updateDNS();
		reg_ra.stopMWI();
    	WifiManager wm = (WifiManager) Receiver.mContext.getSystemService(Context.WIFI_SERVICE);
    	wm.startScan();
	}
	
	public void updateDNS() {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(getUIContext()).edit();
		int i = 0;
		for (SipProvider sip_provider : sip_providers) {
			try {
				edit.putString(Settings.PREF_DNS+i, IpAddress.getByName(PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_SERVER+(i!=0?i:""), "")).toString());
			} catch (UnknownHostException e1) {
				i++;
				continue;
			}
			edit.commit();
			setOutboundProxy(sip_provider,i);
			i++;
		}
	}

	/** Receives incoming calls (auto accept) */
	public void listen() 
	{
		for (UserAgent ua : uas) {
			if (ua != null) {
				ua.printLog("UAS: WAITING FOR INCOMING CALL");
				
				if (!ua.user_profile.audio && !ua.user_profile.video)
				{
					ua.printLog("ONLY SIGNALING, NO MEDIA");
				}
				
				ua.listen();
			}
		}
	}
	
	public void info(char c, int duration) {
		ua.info(c, duration);
	}
	
	/** Makes a new call */
	public boolean call(String target_url,boolean force) {
		int p = pref;
		boolean found = false;
		
		if (isRegistered(p) && Receiver.isFast(p))
			found = true;
		else {
			for (p = 0; p < LINES; p++)
				if (isRegistered(p) && Receiver.isFast(p)) {
					found = true;
					break;
				}
			if (!found && force) {
				p = pref;
				if (Receiver.isFast(p))
					found = true;
				else for (p = 0; p < LINES; p++)
					if (Receiver.isFast(p)) {
						found = true;
						break;
					}
			}
		}
				
		if (!found || (ua = uas[p]) == null) {
			if (PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_CALLBACK, Settings.DEFAULT_CALLBACK) &&
					PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_POSURL, Settings.DEFAULT_POSURL).length() > 0) {
				Receiver.url("n="+Uri.decode(target_url));
				return true;
			}
			return false;
		}

		ua.printLog("UAC: CALLING " + target_url);
		
		if (!ua.user_profile.audio && !ua.user_profile.video)
		{
			 ua.printLog("ONLY SIGNALING, NO MEDIA");
		}
		return ua.call(target_url, false);
	}

	public void answercall() 
	{
		Receiver.stopRingtone();
		ua.accept();
	}

	public void rejectcall() {
		ua.printLog("UA: HANGUP");
		ua.hangup();
	}

	public void togglehold() {
		ua.reInvite(null, 0);
	}

	public void transfer(String number) {
		ua.callTransfer(number, 0);
	}
	
	public void togglemute() {
		if (ua.muteMediaApplication())
			Receiver.onText(Receiver.CALL_NOTIFICATION, getUIContext().getString(R.string.menu_mute), android.R.drawable.stat_notify_call_mute,Receiver.ccCall.base);
		else
			Receiver.progress();
	}
	
	public void togglebluetooth() {
		ua.bluetoothMediaApplication();
		Receiver.progress();
	}
	
	public int speaker(int mode) {
		int ret = ua.speakerMediaApplication(mode);
		Receiver.progress();
		return ret;
	}
	
	public void keepAlive() {
		int i = 0;
		for (KeepAliveSip ka : kas) {
			if (ka != null && Receiver.on_wlan && isRegistered(i))
				try {
					ka.sendToken();
					Receiver.alarm(60, LoopAlarm.class);
				} catch (IOException e) {
					if (!Ruby.release) e.printStackTrace();
				}
			i++;
		}
	}
}

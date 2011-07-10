
package org.zoolu.net;

import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.ruby.client.ui.Receiver;
import org.ruby.client.ui.Settings;
import org.ruby.client.ui.Ruby;

import android.preference.PreferenceManager;
import android.content.Context;

import com.jstun.demo.DiscoveryTest;

/**
 * IpAddress is an IP address.
 */
public class IpAddress {

	/** The host address/name */
	String address;

	/** The InetAddress */
	InetAddress inet_address;
	
	/** Local IP address */
	public static String localIpAddress = "127.0.0.1";
	
	public static Context getUIContext() {
		return Receiver.mContext;
	}
		
	// ********************* Protected *********************

	/** Creates an IpAddress */
	IpAddress(InetAddress iaddress) {
		init(null, iaddress);
	}

	/** Inits the IpAddress */
	private void init(String address, InetAddress iaddress) {
		this.address = address;
		this.inet_address = iaddress;
	}

	/** Gets the InetAddress */
	InetAddress getInetAddress() {
		if (inet_address == null)
			try {
				inet_address = InetAddress.getByName(address);
			} catch (java.net.UnknownHostException e) {
				inet_address = null;
			}
		return inet_address;
	}

	// ********************** Public ***********************

	/** Creates an IpAddress */
	public IpAddress(String address) {
		init(address, null);
	}

	/** Creates an IpAddress */
	public IpAddress(IpAddress ipaddr) {
		init(ipaddr.address, ipaddr.inet_address);
	}

	/** Gets the host address */
	/*
	 * public String getAddress() { if (address==null)
	 * address=inet_address.getHostAddress(); return address; }
	 */

	/** Makes a copy */
	public Object clone() {
		return new IpAddress(this);
	}

	/** Whether it is equal to Object <i>obj</i> */
	public boolean equals(Object obj) {
		try {
			IpAddress ipaddr = (IpAddress) obj;
			if (!toString().equals(ipaddr.toString()))
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** Gets a String representation of the Object */
	public String toString() {
		if (address == null && inet_address != null)
			address = inet_address.getHostAddress();
		return address;
	}

	// *********************** Static ***********************

	/** Gets the IpAddress for a given fully-qualified host name. */
	public static IpAddress getByName(String host_addr)
			throws java.net.UnknownHostException {
		InetAddress iaddr = InetAddress.getByName(host_addr);
		return new IpAddress(iaddr);
	}
	
	/** Sets the local IP address into the variable <i>localIpAddress</i> */
	public static void setLocalIpAddress() {
		localIpAddress = "127.0.0.1";

		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();

				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					if (!inetAddress.isLoopbackAddress()) { 
						if (!PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_STUN, Settings.DEFAULT_STUN)) {
							localIpAddress = inetAddress.getHostAddress().toString();
						} else {
							try {
								String StunServer = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_STUN_SERVER, Settings.DEFAULT_STUN_SERVER);
								int StunServerPort = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_STUN_SERVER_PORT, Settings.DEFAULT_STUN_SERVER_PORT));

								DiscoveryTest StunDiscover = new DiscoveryTest(inetAddress, StunServer, StunServerPort);

								// call out to stun server 
								StunDiscover.test();
								//System.out.println("Public ip is:" + StunDiscover.di.getPublicIP().getHostAddress());
								localIpAddress = StunDiscover.di.getPublicIP().getHostAddress();
							} catch (BindException be) {
								if (!Ruby.release)
									System.out.println(inetAddress.toString() + ": " + be.getMessage());
							} catch (Exception e) {
								if (!Ruby.release) {
									System.out.println(e.getMessage());
									e.printStackTrace();
								}
							} 
						}
					}					
				}
			}
		} catch (SocketException ex) {
			// do nothing
		}
	}
}

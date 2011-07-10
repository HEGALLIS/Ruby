package org.ruby.client;

import org.zoolu.sip.address.NameAddress;

/** Listener of RegisterAgent */
public interface RegisterAgentListener {
	/** When a UA has been successfully (un)registered. */
	public void onUaRegistrationSuccess(RegisterAgent ra, NameAddress target,
			NameAddress contact, String result);

    public void onMWIUpdate(RegisterAgent ra, boolean voicemail, int number, String vmacc);

	/** When a UA failed on (un)registering. */
	public void onUaRegistrationFailure(RegisterAgent ra, NameAddress target,
			NameAddress contact, String result);

}

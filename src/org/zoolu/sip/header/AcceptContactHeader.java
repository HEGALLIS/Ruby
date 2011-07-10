
package org.zoolu.sip.header;

/** 
 * SIP Header AcceptContact
 * 
 * Used with MMTel/IMS. 
 */

public class AcceptContactHeader extends ParametricHeader {
	public AcceptContactHeader(String icsi) {
		super(SipHeaders.Accept_Contact, "*");
		if (icsi != null)
			this.setParameter("+g.3gpp.icsi-ref", icsi);
	}

	public AcceptContactHeader() {
		super(SipHeaders.Accept_Contact, "*");
	}

	public AcceptContactHeader(Header hd) {
		super(hd);
	}


}

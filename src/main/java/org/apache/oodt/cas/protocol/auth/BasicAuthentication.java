package org.apache.oodt.cas.protocol.auth;

/**
 * 
 * @author bfoster
 * @version $Revision$
 */
public class BasicAuthentication implements Authentication {

	private String user;
	private String pass;
	
	public BasicAuthentication(String user, String pass) {
		this.user = user;
		this.pass = pass;
	}
	
	public String user() {
		return user;
	}

	public String pass() {
		return pass;
	}

}

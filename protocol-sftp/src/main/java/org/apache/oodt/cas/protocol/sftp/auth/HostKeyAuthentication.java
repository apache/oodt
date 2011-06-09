package org.apache.oodt.cas.protocol.sftp.auth;

import org.apache.oodt.cas.protocol.auth.BasicAuthentication;

public class HostKeyAuthentication extends BasicAuthentication {

	private String hostKeyFile;
	
	public HostKeyAuthentication(String user, String pass, String hostKeyFile) {
		super(user, pass);
		this.hostKeyFile = hostKeyFile;
	}

	public String getHostKeyFile() {
		return hostKeyFile;
	}
}

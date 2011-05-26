package org.apache.oodt.cas.protocol.verify;

import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;

public class BasicProtocolVerifier implements ProtocolVerifier {

	private static final Logger LOG = Logger.getLogger(BasicProtocolVerifier.class.getName());
	
	private Map<URI, ProtocolFile> uriTestCdMap;
	
	public BasicProtocolVerifier(Map<URI, ProtocolFile> uriTestCdMap) {
		this.uriTestCdMap = uriTestCdMap;
	}

	public boolean verify(Protocol protocol, URI site, Authentication auth) {
        try {
            LOG.log(Level.INFO, "Testing protocol "
                    + protocol.getClass().getCanonicalName()
                    + " . . . this may take a few minutes . . .");
            
            // Test connectivity
            protocol.connect(site.getHost(), auth);
            
            // Test ls, cd, and pwd
            protocol.cd(ProtocolFile.HOME);
            ProtocolFile home = protocol.pwd();
            protocol.ls();
            if (uriTestCdMap.containsKey(site)) {
            	protocol.cd(uriTestCdMap.get(site));
            } else {
            	protocol.cd(ProtocolFile.ROOT);
            }
            protocol.cd(ProtocolFile.HOME);
            
            // Verify again at home directory
            if (home == null || !home.equals(protocol.pwd()))
                throw new ProtocolException(
                        "Home directory not the same after cd");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Protocol "
                    + protocol.getClass().getCanonicalName()
                    + " failed compatibility test : " + e.getMessage(), e);
            return false;
        }
        return true;
    }

}

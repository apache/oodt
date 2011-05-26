package org.apache.oodt.cas.protocol.verify;

import java.net.URI;

import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.auth.Authentication;

public interface ProtocolVerifier {
	
    public boolean verify(Protocol protocol, URI site, Authentication auth);
    
}

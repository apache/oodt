package org.apache.oodt.cas.protocol.system;

import java.net.URI;
import java.util.List;

import org.apache.oodt.cas.protocol.ProtocolFactory;

public interface ProtocolConfig {

	public List<ProtocolFactory> getFactoriesBySite(URI site);
	
}

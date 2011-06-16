package org.apache.oodt.cas.protocol.action;

import org.apache.oodt.cas.protocol.ProtocolFactory;
import org.apache.oodt.cas.protocol.system.ProtocolManager;

public class GetSupportedFactoriesAction extends ProtocolAction {

	@Override
	public void performAction(ProtocolManager protocolManager) throws Exception {
		System.out.println("Supported Factories:");
		for (ProtocolFactory factory : protocolManager.getFactories()) {
			System.out.println(" - " + factory.getClass().getCanonicalName());
		}
	}

}

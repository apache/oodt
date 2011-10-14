package org.apache.oodt.cas.cl.store.spring;

import org.apache.oodt.cas.cl.store.CmdLineStore;
import org.apache.oodt.cas.cl.store.CmdLineStoreFactory;

public class SpringCmdLineStoreFactory implements CmdLineStoreFactory {

	private String config;

	public SpringCmdLineStoreFactory() {
		config = System.getProperty("org.apache.oodt.cas.cl.option.store.spring.config", null);
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public CmdLineStore createStore() {
		if (config != null) {
			return new SpringCmdLineStore(config); 
		} else {
			return null;
		}
	}
}

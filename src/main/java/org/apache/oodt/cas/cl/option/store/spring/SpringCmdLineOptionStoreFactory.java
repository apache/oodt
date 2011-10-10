package org.apache.oodt.cas.cl.option.store.spring;

import org.apache.oodt.cas.cl.option.store.CmdLineOptionStore;
import org.apache.oodt.cas.cl.option.store.CmdLineOptionStoreFactory;

public class SpringCmdLineOptionStoreFactory implements CmdLineOptionStoreFactory {

	private String config;

	public void setConfig(String config) {
		this.config = config;
	}

	public CmdLineOptionStore createStore() {
		if (config != null) {
			return new SpringCmdLineOptionStore(config); 
		} else {
			return null;
		}
	}
}

package org.apache.oodt.cas.cl.store.spring;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.store.CmdLineStore;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringCmdLineStore implements CmdLineStore {

	private ApplicationContext appContext;

	public SpringCmdLineStore(String springConfig) {
		appContext = new FileSystemXmlApplicationContext(springConfig);
		handleSpringSetContextInjectionType();
	}

	private void handleSpringSetContextInjectionType() {
		@SuppressWarnings("unchecked")
		Map<String, SpringSetContextInjectionType> beans = appContext
				.getBeansOfType(SpringSetContextInjectionType.class);
		for (SpringSetContextInjectionType bean : beans.values()) {
			bean.setContext(appContext);
		}
	}

	public Set<CmdLineOption> loadSupportedOptions() {
		@SuppressWarnings("unchecked")
		Map<String, CmdLineOption> optionsMap = appContext
				.getBeansOfType(CmdLineOption.class);
		return new HashSet<CmdLineOption>(optionsMap.values());
	}

	public Set<CmdLineAction> loadSupportedActions() {
		@SuppressWarnings("unchecked")
		Map<String, CmdLineAction> actionsMap = appContext
				.getBeansOfType(CmdLineOption.class);
		return new HashSet<CmdLineAction>(actionsMap.values());
	}
}

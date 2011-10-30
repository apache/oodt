package org.apache.oodt.cas.cl.store.spring;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.validator.CmdLineOptionValidator;
import org.apache.oodt.cas.cl.store.CmdLineStore;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringCmdLineStore implements CmdLineStore {

	private ApplicationContext appContext;

	public SpringCmdLineStore(String springConfig) {
		appContext = new FileSystemXmlApplicationContext(springConfig);
		handleSpringSetContextInjectionType();
		handleSettingNameForCmdLineActions();
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
				.getBeansOfType(CmdLineAction.class);
		return new HashSet<CmdLineAction>(actionsMap.values());
	}

	protected ApplicationContext getApplicationContext() {
		return appContext;
	}
	
	private void handleSpringSetContextInjectionType() {
		@SuppressWarnings("unchecked")
		Map<String, SpringSetContextInjectionType> beans = appContext
				.getBeansOfType(SpringSetContextInjectionType.class);
		@SuppressWarnings("unchecked")
		Map<String, AdvancedCmdLineOption> options = appContext
				.getBeansOfType(AdvancedCmdLineOption.class);
		for (AdvancedCmdLineOption option : options.values()) {
			if (option.hasHandler()
					&& option.getHandler() instanceof SpringSetContextInjectionType) {
				beans.put(UUID.randomUUID().toString(),
						(SpringSetContextInjectionType) option.getHandler());
			} else if (!option.getValidators().isEmpty()) {
				for (CmdLineOptionValidator validator : option.getValidators()) {
					if (validator instanceof SpringSetContextInjectionType) {
						beans.put(UUID.randomUUID().toString(),
								(SpringSetContextInjectionType) validator);
					}
				}
			}
		}
		for (SpringSetContextInjectionType bean : beans.values()) {
			bean.setContext(appContext);
		}
	}

	private void handleSettingNameForCmdLineActions() {
		@SuppressWarnings("unchecked")
		Map<String, CmdLineAction> beans = appContext
		.getBeansOfType(CmdLineAction.class);
		for (Entry<String, CmdLineAction> entry : beans.entrySet()) {
			entry.getValue().setName(entry.getKey());
		}
	}
}

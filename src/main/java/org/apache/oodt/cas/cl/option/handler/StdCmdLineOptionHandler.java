package org.apache.oodt.cas.cl.option.handler;

import static org.apache.oodt.cas.cl.util.CmdLineUtils.convertToType;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;

public class StdCmdLineOptionHandler {

	private Map<String, String> actionToMethodMap;

	public void setActionToMethodMap(Map<String, String> actionToMethodMap) {
		this.actionToMethodMap = actionToMethodMap;
	}

	public void handleOption(CmdLineAction action, CmdLineOptionInstance optionInstance) {
		try {
			Class<?> type = optionInstance.getOption().getType();
			Object[] vals = (optionInstance.getValues().isEmpty()) ? convertToType(
					Arrays.asList(new String[] { "true" }), type = Boolean.TYPE)
					: convertToType(optionInstance.getValues(), type);
			if (actionToMethodMap != null && actionToMethodMap.containsKey(action.getName())) {
				action.getClass()
						.getMethod(actionToMethodMap.get(action.getName()), type)
						.invoke(action, vals);
			} else {
				action
					.getClass()
					.getMethod(
							"set" + StringUtils.capitalize(optionInstance.getOption().getLongOption()), type)
					.invoke(action, vals);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getHelp(CmdLineOption option) {
		return "Will invoke 'set" + option.getLongOption() + "' on action selected";
	}
}

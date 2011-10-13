package org.apache.oodt.cas.cl;

import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findAction;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findActionOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findHelpOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findSpecifiedOption;

import java.util.HashSet;
import java.util.Set;

import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;

public class CmdLineArgs {

	private CmdLineAction specifiedAction;
	private Set<CmdLineAction> supportedActions;

	private CmdLineOption helpOption;
	private CmdLineOptionInstance helpOptionInst;
	private CmdLineOption actionOption;
	private CmdLineOptionInstance actionOptionInst;
	private Set<CmdLineOption> supportedOptions;
	private Set<CmdLineOption> customSupportedOptions;
	private Set<CmdLineOptionInstance> specifiedOptions;
	private Set<CmdLineOptionInstance> customSpecifiedOptions;

	public CmdLineArgs(Set<CmdLineAction> supportedActions, Set<CmdLineOption> supportedOptions, Set<CmdLineOptionInstance> specifiedOptions) {
		helpOption = findHelpOption(supportedOptions);
		helpOptionInst = findSpecifiedOption(helpOption, specifiedOptions);
		actionOption = findActionOption(supportedOptions);
		actionOptionInst = findSpecifiedOption(actionOption, specifiedOptions);

		this.supportedOptions = new HashSet<CmdLineOption>(supportedOptions);

		customSupportedOptions = new HashSet<CmdLineOption>(supportedOptions);
		customSupportedOptions.remove(helpOption);
		customSupportedOptions.remove(actionOption);

		this.specifiedOptions = new HashSet<CmdLineOptionInstance>(specifiedOptions);

		customSpecifiedOptions = new HashSet<CmdLineOptionInstance>(specifiedOptions);
		customSpecifiedOptions.remove(helpOptionInst);
		customSpecifiedOptions.remove(actionOptionInst);

		this.supportedActions = supportedActions;
		if(actionOptionInst != null) {
			specifiedAction = findAction(actionOptionInst, supportedActions);
		}
	}

	public CmdLineOption getHelpOption() {
		return helpOption;
	}

	public CmdLineOptionInstance getHelpOptionInst() {
		return helpOptionInst;
	}

	public CmdLineOption getActionOption() {
		return actionOption;
	}

	public CmdLineOptionInstance getActionOptionInst() {
		return actionOptionInst;
	}

	public Set<CmdLineOption> getSupportedOptions() {
		return supportedOptions;
	}

	public Set<CmdLineOption> getCustomSupportedOptions() {
		return customSupportedOptions;
	}

	public Set<CmdLineOptionInstance> getSpecifiedOptions() {
		return specifiedOptions;
	}

	public Set<CmdLineOptionInstance> getCustomSpecifiedOptions() {
		return customSpecifiedOptions;
	}

	public Set<CmdLineAction> getSupportedActions() {
		return supportedActions;
	}

	public CmdLineAction getSpecifiedAction() {
		return specifiedAction;
	}
}

package org.apache.oodt.cas.cl;

import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findAction;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findActionOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findHelpOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findPrintSupportedActionsOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findSpecifiedOption;

import java.util.HashSet;
import java.util.Set;

import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.ActionCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.HelpCmdLineOption;
import org.apache.oodt.cas.cl.option.PrintSupportedActionsCmdLineOption;

public class CmdLineArgs {

	private CmdLineAction specifiedAction;
	private Set<CmdLineAction> supportedActions;

	private HelpCmdLineOption helpOption;
	private CmdLineOptionInstance helpOptionInst;
	private ActionCmdLineOption actionOption;
	private CmdLineOptionInstance actionOptionInst;
	private PrintSupportedActionsCmdLineOption psaOption;
	private CmdLineOptionInstance psaOptionInst;
	private Set<CmdLineOption> supportedOptions;
	private Set<CmdLineOption> customSupportedOptions;
	private Set<CmdLineOptionInstance> specifiedOptions;
	private Set<CmdLineOptionInstance> customSpecifiedOptions;

	CmdLineArgs(Set<CmdLineAction> supportedActions, Set<CmdLineOption> supportedOptions, Set<CmdLineOptionInstance> specifiedOptions) {
		helpOption = findHelpOption(supportedOptions);
		helpOptionInst = findSpecifiedOption(helpOption, specifiedOptions);
		actionOption = findActionOption(supportedOptions);
		actionOptionInst = findSpecifiedOption(actionOption, specifiedOptions);
		psaOption = findPrintSupportedActionsOption(supportedOptions);
		psaOptionInst = findSpecifiedOption(psaOption, specifiedOptions);

		this.supportedOptions = new HashSet<CmdLineOption>(supportedOptions);

		customSupportedOptions = new HashSet<CmdLineOption>(supportedOptions);
		customSupportedOptions.remove(helpOption);
		customSupportedOptions.remove(actionOption);
		customSupportedOptions.remove(psaOption);

		this.specifiedOptions = new HashSet<CmdLineOptionInstance>(specifiedOptions);

		customSpecifiedOptions = new HashSet<CmdLineOptionInstance>(specifiedOptions);
		if (helpOptionInst != null) {
			customSpecifiedOptions.remove(helpOptionInst);
		}
		if (actionOptionInst != null) {
			customSpecifiedOptions.remove(actionOptionInst);
		}
		if (psaOptionInst != null) {
			customSpecifiedOptions.remove(psaOptionInst);
		}

		this.supportedActions = supportedActions;
		if(actionOptionInst != null) {
			specifiedAction = findAction(actionOptionInst, supportedActions);
		}
	}

	public HelpCmdLineOption getHelpOption() {
		return helpOption;
	}

	public CmdLineOptionInstance getHelpOptionInst() {
		return helpOptionInst;
	}

	public ActionCmdLineOption getActionOption() {
		return actionOption;
	}

	public CmdLineOptionInstance getActionOptionInst() {
		return actionOptionInst;
	}

	public PrintSupportedActionsCmdLineOption getPrintSupportedActionsOption() {
		return psaOption;
	}

	public CmdLineOptionInstance getPrintSupportedActionsOptionInst() {
		return psaOptionInst;
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

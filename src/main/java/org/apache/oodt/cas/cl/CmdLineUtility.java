package org.apache.oodt.cas.cl;

import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.determineRequired;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findActionOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findHelpOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findSpecifiedActionOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findSpecifiedOption;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.help.presenter.CmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.presenter.StdCmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.printer.CmdLineActionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.CmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineActionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.option.ActionCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.HandleableCmdLineOption;
import org.apache.oodt.cas.cl.option.HelpCmdLineOption;
import org.apache.oodt.cas.cl.option.ValidatableCmdLineOption;
import org.apache.oodt.cas.cl.option.require.RequirementRule;
import org.apache.oodt.cas.cl.option.require.RequirementRule.Relation;
import org.apache.oodt.cas.cl.option.store.CmdLineOptionStore;
import org.apache.oodt.cas.cl.option.store.spring.SpringCmdLineOptionStoreFactory;
import org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils;
import org.apache.oodt.cas.cl.option.validator.CmdLineOptionValidator;
import org.apache.oodt.cas.cl.parser.CmdLineOptionParser;
import org.apache.oodt.cas.cl.parser.StdCmdLineOptionParser;

public class CmdLineUtility {

	private CmdLineOptionParser parser;
	private CmdLineOptionStore optionStore;
	private CmdLineOptionHelpPrinter optionHelpPrinter;
	private CmdLineActionHelpPrinter actionHelpPrinter;
	private CmdLineOptionHelpPresenter helpPresenter;
	
	public CmdLineUtility() {
		parser = new StdCmdLineOptionParser();
		optionStore = new SpringCmdLineOptionStoreFactory().createStore();
		optionHelpPrinter = new StdCmdLineOptionHelpPrinter();
		actionHelpPrinter = new StdCmdLineActionHelpPrinter();
		helpPresenter = new StdCmdLineOptionHelpPresenter();
	}

	public CmdLineOptionStore getOptionStore() {
		return optionStore;
	}

	public void setOptionStore(CmdLineOptionStore optionStore) {
		this.optionStore = optionStore;
	}

	public CmdLineOptionHelpPrinter getOptionHelpPrinter() {
		return optionHelpPrinter;
	}

	public void setOptionHelpPrinter(CmdLineOptionHelpPrinter optionHelpPrinter) {
		this.optionHelpPrinter = optionHelpPrinter;
	}

	public CmdLineActionHelpPrinter getActionHelpPrinter() {
		return actionHelpPrinter;
	}

	public void setActionHelpPrinter(CmdLineActionHelpPrinter actionHelpPrinter) {
		this.actionHelpPrinter = actionHelpPrinter;
	}

	public CmdLineOptionHelpPresenter getHelpPresenter() {
		return helpPresenter;
	}

	public void setHelpPresenter(CmdLineOptionHelpPresenter helpPresenter) {
		this.helpPresenter = helpPresenter;
	}

	public void printOptionHelp(Set<CmdLineOption> options) {
		helpPresenter.presentOptionHelp(optionHelpPrinter.printHelp(options));
	}

	public void printActionHelp(CmdLineAction action, Set<CmdLineOption> options) {
		helpPresenter.presentActionHelp(actionHelpPrinter.printHelp(action, options));
	}

	public void run(String[] args) throws IOException {
		Set<CmdLineOptionInstance> specifiedOptions = parse(args);
		if (!handleHelp(specifiedOptions)) {
			execute(specifiedOptions);
		}
	}

	public Set<CmdLineOptionInstance> parse(String[] args) throws IOException {
		Validate.notNull(parser);
		Validate.notNull(optionStore);

		// Load supported options.
		Set<CmdLineOption> validOptions = optionStore.loadSupportedOptions();

		// Insure help options is present if required.
		HelpCmdLineOption helpOption = findHelpOption(validOptions); 
		if (helpOption == null) {
			validOptions.add(helpOption = new HelpCmdLineOption());
		}

		// Insure action options is present if required.
		ActionCmdLineOption actionOption = findActionOption(validOptions); 
		if (actionOption == null) {
			validOptions.add(actionOption = new ActionCmdLineOption());
		}

		// Parse command line arguments.
		return parser.parse(args, validOptions);
	}

	public boolean handleHelp(Set<CmdLineOptionInstance> specifiedOptions) throws IOException {
		Set<CmdLineOption> supportedOptions = optionStore.loadSupportedOptions();
		HelpCmdLineOption helpOption = findHelpOption(supportedOptions); 
		if (helpOption != null) {
			CmdLineOptionInstance specifiedHelpOption = findSpecifiedOption(
					helpOption, specifiedOptions);
			if (specifiedHelpOption != null) {
				if (specifiedHelpOption.getSubOptions().isEmpty()) {
					printOptionHelp(supportedOptions);
				} else {
					printActionHelp(getSelectedAction(specifiedOptions), supportedOptions);
				}
				return true;
			}
		}
		return false;
	}

	public CmdLineAction getSelectedAction(Set<CmdLineOptionInstance> specifiedOptions) throws IOException {
		CmdLineOptionInstance actionOption = findSpecifiedActionOption(specifiedOptions);
		if (actionOption == null) {
			throw new IOException("Action option not specified");
		}
		CmdLineAction action = findAction(actionOption, optionStore.loadSupportedActions());
		if (action == null) {
			throw new IOException("Action '" + actionOption + "' is not a supported action");
		}
		return action;
	}

	public void execute(Set<CmdLineOptionInstance> specifiedOptions) throws IOException {
		CmdLineAction action = getSelectedAction(specifiedOptions);

		Set<CmdLineOption> requiredOptions = determineRequired(action, optionStore.loadSupportedOptions());
		Set<CmdLineOption> requiredOptionsNotSet = check(requiredOptions, specifiedOptions);
		if (!requiredOptionsNotSet.isEmpty()) {
			throw new IOException("Required options are not set: '" + requiredOptionsNotSet + "'");
		}

		Set<CmdLineOptionInstance> optionsFailedValidation = validate(specifiedOptions);
		if (!optionsFailedValidation.isEmpty()) {
			throw new IOException("Options failed validation: '" + optionsFailedValidation + "'");
		}

		handle(action, specifiedOptions);

		action.execute();
	}

	public static Set<CmdLineOption> check(Set<CmdLineOption> requiredOptions, Set<CmdLineOptionInstance> specifiedOptions) {
		HashSet<CmdLineOption> requiredOptionsNotSet = new HashSet<CmdLineOption>(requiredOptions);
		for (CmdLineOptionInstance specifiedOption : specifiedOptions) {
			requiredOptionsNotSet.remove(specifiedOption.getOption());
		}
		return requiredOptionsNotSet;
	}

	public static Set<CmdLineOptionInstance> validate(Set<CmdLineOptionInstance> options)  {
		Validate.notNull(options);

		HashSet<CmdLineOptionInstance> optionsFailed = new HashSet<CmdLineOptionInstance>();
		for (CmdLineOptionInstance optionInst : options) {
			if (!validate(optionInst)) {
				optionsFailed.add(optionInst);
			}
		}
		return optionsFailed;
	}

	public static boolean validate(CmdLineOptionInstance option) {
		if (option.isValidatable()) {
			for (CmdLineOptionValidator validator : ((ValidatableCmdLineOption) option.getOption()).getValidators()) {
				if (!validator.validate(option)) {
					return false;
				}
			}
		}
		return true;
	}

	public static CmdLineAction findAction(CmdLineOptionInstance actionOption, Set<CmdLineAction> supportedActions) {
		Validate.isTrue(actionOption.isAction());
		Validate.notEmpty(actionOption.getValues());

		String actionName = actionOption.getValues().get(0);
		for (CmdLineAction action : supportedActions) {
			if (action.getName().equals(actionName)) {
				return action;
			}
		}
		return null;
	}

	public static void handle(CmdLineAction action, Set<CmdLineOptionInstance> options) {
		for (CmdLineOptionInstance option : options) {
			handle(action, option);
		}
	}

	public static void handle(CmdLineAction action, CmdLineOptionInstance option) {
		if (option.isHandleable()) {
			((HandleableCmdLineOption) option.getOption()).getHandler().handleOption(action, option);
		}
	}
}

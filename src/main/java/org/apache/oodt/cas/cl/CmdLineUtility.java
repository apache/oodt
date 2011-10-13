package org.apache.oodt.cas.cl;

import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.determineRequired;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findActionOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findHelpOption;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.cl.help.presenter.CmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.presenter.StdCmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.printer.CmdLineActionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.CmdLineActionsHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.CmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineActionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.option.ActionCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.HelpCmdLineOption;
import org.apache.oodt.cas.cl.option.PrintSupportedActionsCmdLineOption;
import org.apache.oodt.cas.cl.option.store.CmdLineOptionStore;
import org.apache.oodt.cas.cl.option.store.spring.SpringCmdLineOptionStoreFactory;
import org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils;
import org.apache.oodt.cas.cl.parser.CmdLineOptionParser;
import org.apache.oodt.cas.cl.parser.StdCmdLineOptionParser;

public class CmdLineUtility {

	private CmdLineOptionParser parser;
	private CmdLineOptionStore optionStore;
	private CmdLineOptionHelpPrinter optionHelpPrinter;
	private CmdLineActionHelpPrinter actionHelpPrinter;
	private CmdLineActionsHelpPrinter actionsHelpPrinter;
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

	public CmdLineActionsHelpPrinter getActionsHelpPrinter() {
		return actionsHelpPrinter;
	}

	public void setActionsHelpPrinter(CmdLineActionsHelpPrinter actionsHelpPrinter) {
		this.actionsHelpPrinter = actionsHelpPrinter;
	}

	public CmdLineOptionHelpPresenter getHelpPresenter() {
		return helpPresenter;
	}

	public void setHelpPresenter(CmdLineOptionHelpPresenter helpPresenter) {
		this.helpPresenter = helpPresenter;
	}

	public void printOptionHelp(CmdLineArgs cmdLineArgs) {
		helpPresenter.presentOptionHelp(optionHelpPrinter.printHelp(cmdLineArgs));
	}

	public void printActionHelp(CmdLineArgs cmdLineArgs) {
		helpPresenter.presentActionHelp(actionHelpPrinter.printHelp(cmdLineArgs));
	}

	public void printActionsHelp(CmdLineArgs cmdLineArgs) {
		helpPresenter.presentActionsHelp(actionsHelpPrinter.printHelp(cmdLineArgs));		
	}

	public void run(String[] args) throws IOException {
		CmdLineArgs cmdLineArgs = parse(args);
		if (!handleHelp(cmdLineArgs) && !handlePrintSupportedActions(cmdLineArgs)) {
			execute(cmdLineArgs);
		}
	}

	public CmdLineArgs parse(String[] args) throws IOException {
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

		// Insure print supported actions option is present if required.
		PrintSupportedActionsCmdLineOption psaOption = CmdLineOptionUtils.findPrintSupportedActionsOption(validOptions);
		if (psaOption == null) {
			validOptions.add(psaOption = new PrintSupportedActionsCmdLineOption());
		}

 		// Parse command line arguments.
		return new CmdLineArgs(optionStore.loadSupportedActions(), validOptions, parser.parse(args, validOptions));
	}

	public boolean handleHelp(CmdLineArgs cmdLineArgs) throws IOException {
		if (cmdLineArgs.getHelpOptionInst() != null) {
			if (cmdLineArgs.getHelpOptionInst().getSubOptions().isEmpty()) {
				printOptionHelp(cmdLineArgs);
			} else {
				printActionHelp(cmdLineArgs);
			}
			return true;
		}
		return false;
	}

	public boolean handlePrintSupportedActions(CmdLineArgs cmdLineArgs) throws IOException {
		if (cmdLineArgs.getPrintSupportedActionsOptionInst() != null) {
			printActionsHelp(cmdLineArgs);
			return true;
		}
		return false;
	}

	public static void execute(CmdLineArgs cmdLineArgs) throws IOException {
		Set<CmdLineOption> requiredOptionsNotSet = check(cmdLineArgs);
		if (!requiredOptionsNotSet.isEmpty()) {
			throw new IOException("Required options are not set: '" + requiredOptionsNotSet + "'");
		}

		Set<CmdLineOptionInstance> optionsFailedValidation = validate(cmdLineArgs);
		if (!optionsFailedValidation.isEmpty()) {
			throw new IOException("Options failed validation: '" + optionsFailedValidation + "'");
		}

		handle(cmdLineArgs);

		cmdLineArgs.getSpecifiedAction().execute();
	}

	public static Set<CmdLineOption> check(CmdLineArgs cmdLineArgs) {
		Set<CmdLineOption> requiredOptions = determineRequired(cmdLineArgs.getSpecifiedAction(), cmdLineArgs.getCustomSupportedOptions());
		HashSet<CmdLineOption> requiredOptionsNotSet = new HashSet<CmdLineOption>(requiredOptions);
		for (CmdLineOptionInstance specifiedOption : cmdLineArgs.getCustomSpecifiedOptions()) {
			requiredOptionsNotSet.remove(specifiedOption.getOption());
		}
		return requiredOptionsNotSet;
	}

	public static Set<CmdLineOptionInstance> validate(CmdLineArgs cmdLineArgs)  {
		Validate.notNull(cmdLineArgs);

		HashSet<CmdLineOptionInstance> optionsFailed = new HashSet<CmdLineOptionInstance>();
		for (CmdLineOptionInstance optionInst : cmdLineArgs.getCustomSpecifiedOptions()) {
			if (!CmdLineOptionUtils.validate(optionInst)) {
				optionsFailed.add(optionInst);
			}
		}
		return optionsFailed;
	}

	public static void handle(CmdLineArgs cmdLineArgs) {
		for (CmdLineOptionInstance option : cmdLineArgs.getCustomSpecifiedOptions()) {
			CmdLineOptionUtils.handle(cmdLineArgs.getSpecifiedAction(), option);
		}
	}
}

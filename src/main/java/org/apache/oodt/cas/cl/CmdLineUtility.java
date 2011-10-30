/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.cas.cl;

//OODT static imports
import static org.apache.oodt.cas.cl.util.CmdLineUtils.determineRequired;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.findActionOption;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.findHelpOption;

//JDK imports
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cl.action.store.CmdLineActionStore;
import org.apache.oodt.cas.cl.action.store.spring.SpringCmdLineActionStoreFactory;
import org.apache.oodt.cas.cl.help.presenter.CmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.presenter.StdCmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.printer.CmdLineActionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.CmdLineActionsHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.CmdLineOptionsHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineActionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionsHelpPrinter;
import org.apache.oodt.cas.cl.option.ActionCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.HelpCmdLineOption;
import org.apache.oodt.cas.cl.option.PrintSupportedActionsCmdLineOption;
import org.apache.oodt.cas.cl.option.store.CmdLineOptionStore;
import org.apache.oodt.cas.cl.option.store.spring.SpringCmdLineOptionStoreFactory;
import org.apache.oodt.cas.cl.parser.CmdLineOptionParser;
import org.apache.oodt.cas.cl.parser.StdCmdLineOptionParser;
import org.apache.oodt.cas.cl.util.Args;
import org.apache.oodt.cas.cl.util.CmdLineUtils;

/**
 * A highly configurable utility class which supports parsing and handling of 
 * command line arguments via its action driven design.  After parsing the
 * command line arguments it will check for required arguments not specified,
 * validate the arguments, run the arguments handlers and then invoke the specified
 * action.  It also supports print out help messages and printing supported actions.
 *
 * @author bfoster (Brian Foster)
 */
public class CmdLineUtility {

	private CmdLineOptionParser parser;
	private CmdLineOptionStore optionStore;
	private CmdLineActionStore actionStore;
	private CmdLineOptionsHelpPrinter optionHelpPrinter;
	private CmdLineActionHelpPrinter actionHelpPrinter;
	private CmdLineActionsHelpPrinter actionsHelpPrinter;
	private CmdLineOptionHelpPresenter helpPresenter;
	
	public CmdLineUtility() {
		parser = new StdCmdLineOptionParser();
		optionStore = new SpringCmdLineOptionStoreFactory().createStore();
		actionStore = new SpringCmdLineActionStoreFactory().createStore();
		optionHelpPrinter = new StdCmdLineOptionsHelpPrinter();
		actionHelpPrinter = new StdCmdLineActionHelpPrinter();
		helpPresenter = new StdCmdLineOptionHelpPresenter();
	}

	public CmdLineOptionStore getOptionStore() {
		return optionStore;
	}

	public void setOptionStore(CmdLineOptionStore optionStore) {
		this.optionStore = optionStore;
	}

	public CmdLineActionStore getActionStore() {
		return actionStore;
	}

	public void setActionStore(CmdLineActionStore actionStore) {
		this.actionStore = actionStore;
	}

	public CmdLineOptionsHelpPrinter getOptionHelpPrinter() {
		return optionHelpPrinter;
	}

	public void setOptionHelpPrinter(CmdLineOptionsHelpPrinter optionHelpPrinter) {
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
		helpPresenter.presentOptionHelp(optionHelpPrinter.printHelp(cmdLineArgs
				.getSupportedOptions()));
	}

	public void printActionHelp(CmdLineArgs cmdLineArgs) {
		Validate.notEmpty(cmdLineArgs.getHelpOptionInst().getValues());

		helpPresenter.presentActionHelp(actionHelpPrinter.printHelp(
				CmdLineUtils.findAction(cmdLineArgs.getHelpOptionInst().getValues()
						.get(0), cmdLineArgs.getSupportedActions()),
				cmdLineArgs.getCustomSupportedOptions()));
	}

	public void printActionsHelp(CmdLineArgs cmdLineArgs) {
		helpPresenter.presentActionsHelp(actionsHelpPrinter.printHelp(cmdLineArgs
				.getSupportedActions()));		
	}

	/**
	 * Parses given command line arguments, then checks for help and print supported actions
	 * options, prints them out if found, otherwise performs execution on the arguments - 
	 * see execute(CmdLineArgs).
	 *
	 * @param args The who will be parsed and executed.
	 * @throws IOException On error parsing or executing the args.
	 */
	public void run(String[] args) throws IOException {
		CmdLineArgs cmdLineArgs = parse(args);
		if (!handleHelp(cmdLineArgs) && !handlePrintSupportedActions(cmdLineArgs)) {
			execute(cmdLineArgs);
		}
	}

	/**
	 * Parses the given command line arguments and converts it to {@link CmdLineArgs}.
	 *
	 * @param args The command line arguments to parse.
	 * @return The parsed command line arguments in {@link CmdLineArgs} form.
	 * @throws IOException On error parsing command line arguments.
	 */
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
		PrintSupportedActionsCmdLineOption psaOption = CmdLineUtils.findPrintSupportedActionsOption(validOptions);
		if (psaOption == null) {
			validOptions.add(psaOption = new PrintSupportedActionsCmdLineOption());
		}

 		// Parse command line arguments.
		return new CmdLineArgs(actionStore.loadSupportedActions(), validOptions, parser.parse(new Args(args), validOptions));
	}

	/**
	 * Checks if help option was specified and if so prints out help.
	 *
	 * @param cmdLineArgs The {@link CmdLineArgs} which will be checked for help option
	 * @return True if help was printed, false otherwise
	 */
	public boolean handleHelp(CmdLineArgs cmdLineArgs) {
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

	/**
	 * Checks if print supported actions option was specified and if so prints out supported actions.
	 *
	 * @param cmdLineArgs The {@link CmdLineArgs} which will be checked for print supported action options
	 * @return True if supported actions was printed, false otherwise
	 */
	public boolean handlePrintSupportedActions(CmdLineArgs cmdLineArgs) {
		if (cmdLineArgs.getPrintSupportedActionsOptionInst() != null) {
			printActionsHelp(cmdLineArgs);
			return true;
		}
		return false;
	}

	/**
	 * Checks if required options are set and validation passes, then runs handlers and executes its action.
	 *
	 * @param cmdLineArgs The {@link CmdLineArgs} for which execution processing will be run.
	 * @throws IOException If required options are missing or validation fails.
	 */
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

	/**
	 * Checks for required options which are not set and returns the ones it finds.
	 *
	 * @param cmdLineArgs The {@link CmdLineArgs} which will be check for required options.
	 * @return The required {@link CmdLineOption}s not specified.
	 */
	public static Set<CmdLineOption> check(CmdLineArgs cmdLineArgs) {
		Set<CmdLineOption> requiredOptions = determineRequired(cmdLineArgs.getSpecifiedAction(), cmdLineArgs.getCustomSupportedOptions());
		HashSet<CmdLineOption> requiredOptionsNotSet = new HashSet<CmdLineOption>(requiredOptions);
		for (CmdLineOptionInstance specifiedOption : cmdLineArgs.getCustomSpecifiedOptions()) {
			requiredOptionsNotSet.remove(specifiedOption.getOption());
		}
		return requiredOptionsNotSet;
	}

	/**
	 * Runs validation on {@link CmdLineArgs} and returns the options which failed validation.
	 *
	 * @param cmdLineArgs The {@link CmdLineArgs} which will be validated. 
	 * @return The {@link CmdLineOptionInstance}s which failed validation.
	 */
	public static Set<CmdLineOptionInstance> validate(CmdLineArgs cmdLineArgs)  {
		Validate.notNull(cmdLineArgs);

		HashSet<CmdLineOptionInstance> optionsFailed = new HashSet<CmdLineOptionInstance>();
		for (CmdLineOptionInstance optionInst : cmdLineArgs.getCustomSpecifiedOptions()) {
			if (!CmdLineUtils.validate(optionInst)) {
				optionsFailed.add(optionInst);
			}
		}
		return optionsFailed;
	}

	/**
	 * Runs the {@link CmdLineOptionHandler}s for {@link CmdLineArgs} given.
	 * @param cmdLineArgs The {@link CmdLineArgs} whose option handlers will be run.
	 */
	public static void handle(CmdLineArgs cmdLineArgs) {
		for (CmdLineOptionInstance option : cmdLineArgs.getCustomSpecifiedOptions()) {
			CmdLineUtils.handle(cmdLineArgs.getSpecifiedAction(), option);
		}
	}
}

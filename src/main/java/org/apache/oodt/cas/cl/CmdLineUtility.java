package org.apache.oodt.cas.cl;

import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.findSpecifiedHelpOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.isHelpOption;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.validateAndHandleInstances;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.cl.help.formatter.CmdLineOptionHelpFormatter;
import org.apache.oodt.cas.cl.help.formatter.StdCmdLineOptionHelpFormatter;
import org.apache.oodt.cas.cl.help.presenter.CmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.presenter.StdCmdLineOptionHelpPresenter;
import org.apache.oodt.cas.cl.help.printer.CmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.CmdLineOptionSpecificHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.StdCmdLineOptionSpecificHelpPrinter;
import org.apache.oodt.cas.cl.option.ActionCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.GroupCmdLineOption;
import org.apache.oodt.cas.cl.option.HelpCmdLineOption;
import org.apache.oodt.cas.cl.option.store.CmdLineOptionStore;
import org.apache.oodt.cas.cl.option.store.spring.SpringCmdLineOptionStoreFactory;
import org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils;
import org.apache.oodt.cas.cl.parser.CmdLineOptionParser;
import org.apache.oodt.cas.cl.parser.StdCmdLineOptionParser;

public class CmdLineUtility {

	private CmdLineOptionParser parser;
	private CmdLineOptionStore optionStore;
	private CmdLineOptionHelpPrinter helpPrinter;
	private CmdLineOptionSpecificHelpPrinter specificHelpPrinter;
	private CmdLineOptionHelpFormatter helpFormatter;
	private CmdLineOptionHelpPresenter helpPresenter;
	private boolean requireHelp;
	private boolean requireAction;
	
	public CmdLineUtility() {
		parser = new StdCmdLineOptionParser();
		optionStore = new SpringCmdLineOptionStoreFactory().createStore();
		helpPrinter = new StdCmdLineOptionHelpPrinter();
		specificHelpPrinter = new StdCmdLineOptionSpecificHelpPrinter();
		helpFormatter = new StdCmdLineOptionHelpFormatter();
		helpPresenter = new StdCmdLineOptionHelpPresenter();
	}

	public CmdLineOptionStore getOptionStore() {
		return optionStore;
	}

	public void setOptionStore(CmdLineOptionStore optionStore) {
		this.optionStore = optionStore;
	}

	public CmdLineOptionHelpPrinter getHelpPrinter() {
		return helpPrinter;
	}

	public void setHelpPrinter(CmdLineOptionHelpPrinter helpPrinter) {
		this.helpPrinter = helpPrinter;
	}

	public CmdLineOptionSpecificHelpPrinter getSpecificHelpPrinter() {
		return specificHelpPrinter;
	}

	public void setSpecificHelpPrinter(
			CmdLineOptionSpecificHelpPrinter specificHelpPrinter) {
		this.specificHelpPrinter = specificHelpPrinter;
	}

	public CmdLineOptionHelpFormatter getHelpFormatter() {
		return helpFormatter;
	}

	public void setHelpFormatter(CmdLineOptionHelpFormatter helpFormatter) {
		this.helpFormatter = helpFormatter;
	}

	public CmdLineOptionHelpPresenter getHelpPresenter() {
		return helpPresenter;
	}

	public void setHelpPresenter(CmdLineOptionHelpPresenter helpPresenter) {
		this.helpPresenter = helpPresenter;
	}

	private void printHelp(Set<CmdLineOption> validOptions) {
		helpPresenter.presentHelp(helpFormatter.format(helpPrinter, validOptions));
	}

	private void printSpecificHelp(Set<CmdLineOption> validOptions, CmdLineOptionInstance specifiedOption) {
		helpPresenter.presentSpecificHelp(helpFormatter.format(
				specificHelpPrinter, validOptions, specifiedOption));
	}

	public void handleCmdLine(String[] args) throws IOException {
		Validate.notNull(parser);
		Validate.notNull(optionStore);

		// Load supported options.
		Set<CmdLineOption> validOptions = optionStore.loadSupportedOptions();
		if (requireHelp) {
			ensureHasHelpOption(validOptions);
		}
		if (requireAction) {
			ensureHasActionOption(validOptions);
		}
		
		// Parse command line arguments.
		Set<CmdLineOptionInstance> specifiedOptions = parser.parse(args, validOptions);

		// Check for help option and handle it if specified then return.
		GroupCmdLineOptionInstance helpOption = findSpecifiedHelpOption(specifiedOptions);
		if (helpOption != null) {
			if (helpOption.getValues().isEmpty()) {
				printHelp(validOptions);
			} else {
				printSpecificHelp(validOptions, helpOption.getValues().get(0));
			}
			return;
		}

		// Otherwise, validate and handle options.
		validateAndHandleInstances(validOptions, specifiedOptions);
	}

	private void ensureHasHelpOption(Set<CmdLineOption> options) {
		Validate.notNull(options);

		if (CmdLineOptionUtils.findHelpOption(options) == null) {
			options.add(new HelpCmdLineOption());
		}
	}

	private void ensureHasActionOption(Set<CmdLineOption> options) {
		Validate.notNull(options);

		if (CmdLineOptionUtils.findActionOption(options) == null) {
			options.add(new ActionCmdLineOption());
		}
	}
}

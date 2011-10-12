package org.apache.oodt.cas.cl;

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
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.store.CmdLineOptionStore;
import org.apache.oodt.cas.cl.option.store.spring.SpringCmdLineOptionStoreFactory;
import org.apache.oodt.cas.cl.parser.CmdLineOptionParser;
import org.apache.oodt.cas.cl.parser.StdCmdLineOptionParser;

public class CmdLineUtility {

	private CmdLineOptionParser parser;
	private CmdLineOptionStore optionStore;
	private CmdLineOptionHelpPrinter helpPrinter;
	private CmdLineOptionSpecificHelpPrinter specificHelpPrinter;
	private CmdLineOptionHelpFormatter helpFormatter;
	private CmdLineOptionHelpPresenter helpPresenter;

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

	public void handleCmdLine(String[] args) {
		Validate.notNull(parser);
		Validate.notNull(optionStore);

		Set<CmdLineOption> validOptions = optionStore.loadSupportedOptions();
		ensureHasHelpOption(validOptions);
//		Set<? extends CmdLineOptionInstance<?>> specifiedOptions = parser.parse(args, validOptions);
//
//		if (specifiedOptions.size() == 1 && specifiedOptions.iterator().next().getOption().isHelp()) {
//			if (args.length > j + 1) {
//				String[] helpArgs = new String[args.length - j - 1];
//				System.arraycopy(args, j + 1, helpArgs, 0, helpArgs.length);
//				helpPresenter.presentSpecificHelp(helpFormatter.format(
//						specificHelpPrinter, validOptions, parse(helpArgs).iterator()
//								.next()));
//			} else {
//				helpPresenter.presentHelp(helpFormatter.format(helpPrinter,
//						validOptions));
//			}	
//		}
//		// check if is a perform and quit option
//		if (curOption.isPerformAndQuit()) {
//			curOption.getHandler().handleOption(curOption, values);
//			System.exit(0);
//		}
		
	}

	private void ensureHasHelpOption(Set<CmdLineOption> validOptions) {
		Validate.notNull(validOptions);

		if (validOptions != null) {
			for (CmdLineOption validOption : validOptions) {
				if (validOption.isHelp()) {
					return;
				}
			}
		}

		CmdLineOption helpOption = new CmdLineOption("h", "help", "Prints Help Menu", false);
		helpOption.setHelp(true);
		validOptions.add(helpOption);
	}
}

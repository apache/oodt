package org.apache.oodt.cas.cl.help.formatter;

import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.getAlwaysRequiredOptions;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.getConditionallyRequiredOptions;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.getOptionalOptions;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.sortOptionsByRequiredStatus;

import java.util.List;
import java.util.Set;

import org.apache.oodt.cas.cl.help.printer.CmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.CmdLineOptionSpecificHelpPrinter;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;


public class StdCmdLineOptionHelpFormatter implements CmdLineOptionHelpFormatter {

	public String format(CmdLineOptionHelpPrinter helpPrinter,
			Set<CmdLineOption> options) {
		StringBuffer sb = new StringBuffer("");
		List<CmdLineOption> sortedOptions = sortOptionsByRequiredStatus(options);
		sb.append(helpPrinter.getHeader()).append("\n");
		for (CmdLineOption option : sortedOptions) {
			sb.append(helpPrinter.getOptionHelp(option)).append("\n");
		}
		sb.append(helpPrinter.getFooter()).append("\n");
		return sb.toString();
	}

	public String format(CmdLineOptionSpecificHelpPrinter specificHelpPrinter,
			Set<CmdLineOption> options, CmdLineOptionInstance specifiedOption) {
		StringBuffer sb = new StringBuffer("");
		sb.append(specificHelpPrinter.getHeader(specifiedOption)).append("\n");

		sb.append(specificHelpPrinter.getRequiredSubHeader(specifiedOption)).append("\n");
		Set<CmdLineOption> requiredOptions = getAlwaysRequiredOptions(options);
		requiredOptions.addAll(getConditionallyRequiredOptions(options, specifiedOption));
		List<CmdLineOption> sortedRequiredOptions = sortOptionsByRequiredStatus(requiredOptions);
		for (CmdLineOption option : sortedRequiredOptions) {
			sb.append(specificHelpPrinter.getRequiredOptionHelp(option, specifiedOption)).append("\n");
		}

		sb.append(specificHelpPrinter.getOptionalSubHeader(specifiedOption)).append("\n");
		Set<CmdLineOption> optionalOptions = getOptionalOptions(options, specifiedOption);
		List<CmdLineOption> sortedOptionalOptions = sortOptionsByRequiredStatus(optionalOptions);
		for (CmdLineOption option : sortedOptionalOptions) {
			sb.append(specificHelpPrinter.getOptionalOptionHelp(option, specifiedOption)).append("\n");
		}

		sb.append(specificHelpPrinter.getFooter(specifiedOption)).append("\n");
		return sb.toString();
	}
}

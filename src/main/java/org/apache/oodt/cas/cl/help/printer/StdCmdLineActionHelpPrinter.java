package org.apache.oodt.cas.cl.help.printer;

import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.determineOptional;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.determineRequired;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.sortOptionsByRequiredStatus;

import java.util.List;
import java.util.Set;

import org.apache.oodt.cas.cl.CmdLineUtility;
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils;

public class StdCmdLineActionHelpPrinter implements
		CmdLineActionHelpPrinter {

	public String printHelp(CmdLineAction action, Set<CmdLineOption> options) {
		StringBuffer sb = new StringBuffer("");
		sb.append(getHeader(action)).append("\n");

		sb.append(getRequiredSubHeader()).append("\n");
		Set<CmdLineOption> requiredOptions = determineRequired(action, options);
		List<CmdLineOption> sortedRequiredOptions = sortOptionsByRequiredStatus(requiredOptions);
		for (CmdLineOption option : sortedRequiredOptions) {
			sb.append(getRequiredOptionHelp(option)).append("\n");
		}

		sb.append(getOptionalSubHeader()).append("\n");
		Set<CmdLineOption> optionalOptions = determineOptional(action, options);
		List<CmdLineOption> sortedOptionalOptions = sortOptionsByRequiredStatus(optionalOptions);
		for (CmdLineOption option : sortedOptionalOptions) {
			sb.append(getOptionalOptionHelp(option)).append("\n");
		}

		sb.append(getFooter(action)).append("\n");
		return sb.toString();
	}

	protected String getHeader(CmdLineAction action) {
		return "Action Help for '" + action.getName() + "'";
	}

	protected String getRequiredSubHeader() {
		return " - Required:";
	}

	protected String getRequiredOptionHelp(CmdLineOption option) {
		return getOptionHelp(option);
	}

	protected String getOptionalSubHeader() {
		return " - Optional:";
	}

	protected String getOptionalOptionHelp(CmdLineOption option) {
		return getOptionHelp(option);
	}

	protected String getFooter(CmdLineAction action) {
		return "";
	}

	protected String getOptionHelp(CmdLineOption option) {
		return "    -" + option.getShortOption() + " [--" + option.getLongOption()
				+ "] "
				+ (option.hasArgs() ? "<" + option.getArgsDescription() + ">" : "");
	}
}

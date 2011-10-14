package org.apache.oodt.cas.cl.help.printer;

import static org.apache.oodt.cas.cl.util.CmdLineUtils.determineOptional;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.determineRequired;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.sortOptionsByRequiredStatus;

import java.util.List;
import java.util.Set;

import org.apache.oodt.cas.cl.CmdLineArgs;
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;

public class StdCmdLineActionHelpPrinter implements
		CmdLineActionHelpPrinter {

	public String printHelp(CmdLineArgs cmdLineArgs) {
		StringBuffer sb = new StringBuffer("");
		sb.append(getHeader(cmdLineArgs.getSpecifiedAction())).append("\n");

		sb.append(getRequiredSubHeader()).append("\n");
		Set<CmdLineOption> requiredOptions = determineRequired(cmdLineArgs.getSpecifiedAction(), cmdLineArgs.getCustomSupportedOptions());
		List<CmdLineOption> sortedRequiredOptions = sortOptionsByRequiredStatus(requiredOptions);
		for (CmdLineOption option : sortedRequiredOptions) {
			sb.append(getRequiredOptionHelp(option)).append("\n");
		}

		sb.append(getOptionalSubHeader()).append("\n");
		Set<CmdLineOption> optionalOptions = determineOptional(cmdLineArgs.getSpecifiedAction(), cmdLineArgs.getCustomSupportedOptions());
		List<CmdLineOption> sortedOptionalOptions = sortOptionsByRequiredStatus(optionalOptions);
		for (CmdLineOption option : sortedOptionalOptions) {
			sb.append(getOptionalOptionHelp(option)).append("\n");
		}

		sb.append(getFooter(cmdLineArgs.getSpecifiedAction())).append("\n");
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

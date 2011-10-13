package org.apache.oodt.cas.cl.help.printer;

import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;

public class StdCmdLineOptionSpecificHelpPrinter implements
		CmdLineOptionSpecificHelpPrinter {

	public String getHeader(CmdLineOptionInstance specifiedOption) {
		return "Option Help when option '--"
				+ specifiedOption.getOption().getLongOption()
				+ "' is specified with values " + specifiedOption.getValues();
	}

	public String getRequiredSubHeader(CmdLineOptionInstance specifiedOption) {
		return " - Required:";
	}

	public String getRequiredOptionHelp(CmdLineOption option,
			CmdLineOptionInstance specifiedOption) {
		return getOptionHelp(option, specifiedOption);
	}

	public String getOptionalSubHeader(CmdLineOptionInstance specifiedOption) {
		return " - Optional:";
	}

	public String getOptionalOptionHelp(CmdLineOption option,
			CmdLineOptionInstance specifiedOption) {
		return getOptionHelp(option, specifiedOption);
	}

	public String getFooter(CmdLineOptionInstance specifiedOption) {
		return "";
	}

	private String getOptionHelp(CmdLineOption option,
			CmdLineOptionInstance specifiedOption) {
		return "    -" + option.getShortOption() + " [--" + option.getLongOption()
				+ "] "
				+ (option.hasArgs() ? "<" + option.getArgsDescription() + ">" : "");
	}
}

package org.apache.oodt.cas.cl.help.printer;

import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.getFormattedString;

import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.cl.option.CmdLineOption;

public class StdCmdLineOptionHelpPrinter implements CmdLineOptionHelpPrinter {

	public String getHeader() {
		StringBuffer sb = new StringBuffer("");
		sb.append("-----------------------------------------------------------------------------------------------------------------\n");
		sb.append("|" + StringUtils.rightPad(" Short", 7) + "|"
				+ StringUtils.rightPad(" Long", 50) + "| Description\n");
		sb.append("-----------------------------------------------------------------------------------------------------------------\n");
		return sb.toString();
	}

	public String getOptionHelp(CmdLineOption option) {
		String argName = option.hasArgs() ? " <" + option.getOptionArgName() + ">" : "";
		String optionUsage = "-"
				+ StringUtils.rightPad(option.getShortOption() + ",", 7) + "--"
				+ StringUtils.rightPad((option.getLongOption() + argName), 49)
				+ option.getDescription();
		optionUsage += getFormattedString(option.getHandler().getCustomOptionHelp(option),
				62, 113);

		if (option.isRequired()) {
			optionUsage = " " + optionUsage;
		} else if (!option.getRequirementRules().isEmpty()) {
			optionUsage = "{" + optionUsage + "}";
			optionUsage += "\n" + getFormattedString("RequiredOptions: "
					+ option.getRequirementRules(), 62, 113);
		} else {
			optionUsage = "[" + optionUsage + "]";
		}

		return optionUsage;
	}

	public String getFooter() {
		return "-----------------------------------------------------------------------------------------------------------------";
	}
}

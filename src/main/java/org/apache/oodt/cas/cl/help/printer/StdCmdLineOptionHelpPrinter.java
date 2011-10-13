package org.apache.oodt.cas.cl.help.printer;

import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.getFormattedString;
import static org.apache.oodt.cas.cl.option.util.CmdLineOptionUtils.sortOptionsByRequiredStatus;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.cl.CmdLineArgs;
import org.apache.oodt.cas.cl.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;

public class StdCmdLineOptionHelpPrinter implements CmdLineOptionHelpPrinter {

	public String printHelp(CmdLineArgs cmdLineArgs) {
		StringBuffer sb = new StringBuffer("");
		List<CmdLineOption> sortedOptions = sortOptionsByRequiredStatus(cmdLineArgs.getSupportedOptions());
		sb.append(getHeader()).append("\n");
		for (CmdLineOption option : sortedOptions) {
			sb.append(getOptionHelp(option)).append("\n");
		}
		sb.append(getFooter()).append("\n");
		return sb.toString();
	}

	protected String getHeader() {
		StringBuffer sb = new StringBuffer("");
		sb.append("-----------------------------------------------------------------------------------------------------------------\n");
		sb.append("|" + StringUtils.rightPad(" Short", 7) + "|"
				+ StringUtils.rightPad(" Long", 50) + "| Description\n");
		sb.append("-----------------------------------------------------------------------------------------------------------------\n");
		return sb.toString();
	}

	protected String getOptionHelp(CmdLineOption option) {
		String argName = option.hasArgs() ? " <" + option.getArgsDescription() + ">" : "";
		String optionUsage = "-"
				+ StringUtils.rightPad(option.getShortOption() + ",", 7) + "--"
				+ StringUtils.rightPad((option.getLongOption() + argName), 49)
				+ option.getDescription();
		if (option instanceof AdvancedCmdLineOption) {
			if (((AdvancedCmdLineOption) option).hasHandler()) {
				optionUsage += getFormattedString(((AdvancedCmdLineOption) option).getHandler().getHelp(option),
						62, 113);
			}
		}

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

	protected String getFooter() {
		return "-----------------------------------------------------------------------------------------------------------------";
	}
}

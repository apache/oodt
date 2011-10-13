package org.apache.oodt.cas.cl.option;

public class HelpCmdLineOption extends GroupCmdLineOption {

	public HelpCmdLineOption() {
		this("h", "help", "Prints help menu", false);
	}

	public HelpCmdLineOption(String shortOption, String longOption,
			String description, boolean hasArgs) {
		super(shortOption, longOption, description, hasArgs);
	}
}

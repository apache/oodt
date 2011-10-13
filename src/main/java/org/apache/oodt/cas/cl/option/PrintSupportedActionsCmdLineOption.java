package org.apache.oodt.cas.cl.option;

public class PrintSupportedActionsCmdLineOption extends SimpleCmdLineOption {

	public PrintSupportedActionsCmdLineOption() {
		super("psa", "printSupportedActions", "Print Supported Actions", false);
	}

	public PrintSupportedActionsCmdLineOption(String shortOption, String longOption,
			String description, boolean hasArgs) {
		super(shortOption, longOption, description, hasArgs);
	}
}

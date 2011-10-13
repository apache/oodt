package org.apache.oodt.cas.cl.option;

public class ActionCmdLineOption extends SimpleCmdLineOption {

	public ActionCmdLineOption() {
		this("a", "action", "This is the name of the action to trigger", true);
	}

	public ActionCmdLineOption(String shortOption, String longOption, String description, boolean hasArgs) {
		super(shortOption, longOption, description, hasArgs);
		this.setArgsDescription("action-name");
		this.setRequired(true);
		this.setType(String.class);
		this.setPerformAndQuit(false);
	}

}

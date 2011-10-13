package org.apache.oodt.cas.cl.help.presenter;

import java.io.PrintStream;

public class StdCmdLineOptionHelpPresenter implements CmdLineOptionHelpPresenter {

	private PrintStream ps;

	public StdCmdLineOptionHelpPresenter() {
		ps = new PrintStream(System.out);
	}

	public void presentOptionHelp(String optionHelpMessage) {
		ps.println(optionHelpMessage);
	}

	public void presentActionHelp(String actionHelpMessage) {
		ps.println(actionHelpMessage);
	}

	public void presentActionsHelp(String actionsHelpMessage) {
		ps.println(actionsHelpMessage);		
	}
}

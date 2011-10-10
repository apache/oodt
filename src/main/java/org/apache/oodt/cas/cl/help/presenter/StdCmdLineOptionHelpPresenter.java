package org.apache.oodt.cas.cl.help.presenter;

import java.io.PrintStream;

public class StdCmdLineOptionHelpPresenter implements CmdLineOptionHelpPresenter {

	private PrintStream ps;

	public StdCmdLineOptionHelpPresenter() {
		ps = new PrintStream(System.out);
	}

	public void presentHelp(String helpMessage) {
		ps.println(helpMessage);
	}

	public void presentSpecificHelp(String specificHelpMessage) {
		ps.println(specificHelpMessage);
	}

}

package org.apache.oodt.cas.cl.help.printer;

import org.apache.oodt.cas.cl.CmdLineArgs;
import org.apache.oodt.cas.cl.action.CmdLineAction;

public class StdCmdLineActionsHelpPrinter implements CmdLineActionsHelpPrinter {

	public String printHelp(CmdLineArgs cmdLineArgs) {
		CmdLineAction action = cmdLineArgs.getSpecifiedAction();
		return 
			"Actions:\n" + 
			"  Action:\n    Name: " + action.getName() + "\n" +
			"    Description: " + action.getDescription() + "\n";
	}

}

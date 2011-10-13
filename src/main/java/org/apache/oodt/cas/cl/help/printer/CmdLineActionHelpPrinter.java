package org.apache.oodt.cas.cl.help.printer;

import java.util.Set;

import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;

public interface CmdLineActionHelpPrinter {

	public String printHelp(CmdLineAction action, Set<CmdLineOption> options);

}

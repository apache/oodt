package org.apache.oodt.cas.cl.help.printer;

import java.util.Set;

import org.apache.oodt.cas.cl.option.CmdLineOption;

public interface CmdLineOptionHelpPrinter {

	public String printHelp(Set<CmdLineOption> options);

}

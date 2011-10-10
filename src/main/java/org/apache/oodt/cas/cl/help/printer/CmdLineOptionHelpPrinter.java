package org.apache.oodt.cas.cl.help.printer;

import org.apache.oodt.cas.cl.option.CmdLineOption;

public interface CmdLineOptionHelpPrinter {

	public String getHeader();

	public String getOptionHelp(CmdLineOption option);

	public String getFooter();

}

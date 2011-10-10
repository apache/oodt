package org.apache.oodt.cas.cl.help.printer;

import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;

public interface CmdLineOptionSpecificHelpPrinter {

	public String getHeader(CmdLineOptionInstance specifiedOption);

	public String getRequiredSubHeader(CmdLineOptionInstance specifiedOption);

	public String getRequiredOptionHelp(CmdLineOption option, CmdLineOptionInstance specifiedOption);

	public String getOptionalSubHeader(CmdLineOptionInstance specifiedOption);

	public String getOptionalOptionHelp(CmdLineOption option, CmdLineOptionInstance specifiedOption);

	public String getFooter(CmdLineOptionInstance specifiedOption);

}

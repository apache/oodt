package org.apache.oodt.cas.cl.help.formatter;

import java.util.Set;

import org.apache.oodt.cas.cl.help.printer.CmdLineOptionHelpPrinter;
import org.apache.oodt.cas.cl.help.printer.CmdLineOptionSpecificHelpPrinter;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;

public interface CmdLineOptionHelpFormatter {

	public String format(CmdLineOptionHelpPrinter helpPrinter,
			Set<CmdLineOption> options);

	public String format(CmdLineOptionSpecificHelpPrinter specificHelpPrinter,
			Set<CmdLineOption> options, CmdLineOptionInstance specifiedOption);

}

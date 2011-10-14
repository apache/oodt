package org.apache.oodt.cas.cl.parser;

import java.io.IOException;
import java.util.Set;

import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.util.Args;

public interface CmdLineOptionParser {

	public Set<CmdLineOptionInstance> parse(Args args, Set<CmdLineOption> validOptions) throws IOException;

}

package org.apache.oodt.cas.cl.option;

import org.apache.oodt.cas.cl.option.handler.CmdLineOptionHandler;

public interface HandleableCmdLineOption {

	public void setHandler(CmdLineOptionHandler handler);

	public CmdLineOptionHandler getHandler();
}

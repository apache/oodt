package org.apache.oodt.cas.cl.option.store;

import java.util.Set;

import org.apache.oodt.cas.cl.option.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;

public interface CmdLineOptionStore {

	public Set<CmdLineOption> loadSupportedOptions();

	public Set<CmdLineAction> loadSupportedActions();
}

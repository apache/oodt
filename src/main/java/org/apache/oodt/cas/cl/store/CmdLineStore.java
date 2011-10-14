package org.apache.oodt.cas.cl.store;

import java.util.Set;

import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;

public interface CmdLineStore {

	public Set<CmdLineOption> loadSupportedOptions();

	public Set<CmdLineAction> loadSupportedActions();
}

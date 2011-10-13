package org.apache.oodt.cas.cl.option;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.oodt.cas.cl.option.handler.CmdLineOptionHandler;
import org.apache.oodt.cas.cl.option.validator.CmdLineOptionValidator;

public class AdvancedCmdLineOption extends SimpleCmdLineOption implements ValidatableCmdLineOption, HandleableCmdLineOption {

	private boolean isAction;
	private CmdLineOptionHandler handler;
	private List<CmdLineOptionValidator> validators;
	private boolean performAndQuit;

	public AdvancedCmdLineOption() {
		super();
		validators = new ArrayList<CmdLineOptionValidator>();
	}

	public AdvancedCmdLineOption(String shortOption, String longOption, String description, boolean hasArgs) {
		super(shortOption, longOption, description, hasArgs);
	}

	public void setAction(boolean isAction) {
		this.isAction = isAction;
	}

	public boolean isAction() {
		return isAction;
	}

	public void setHandler(CmdLineOptionHandler handler) {
		this.handler = handler;
	}

	public CmdLineOptionHandler getHandler() {
		return handler;
	}

	public boolean hasHandler() {
		return handler != null;
	}

	public List<CmdLineOptionValidator> getValidators() {
		return this.validators;
	}

	public void setValidators(List<CmdLineOptionValidator> validators) {
		this.validators = validators;
	}

	public boolean isPerformAndQuit() {
		return performAndQuit;
	}

	public void setPerformAndQuit(boolean performAndQuit) {
		this.performAndQuit = performAndQuit;
	}
}

package org.apache.oodt.cas.cl.option;

import java.util.List;

import org.apache.oodt.cas.cl.option.validator.CmdLineOptionValidator;

public interface ValidatableCmdLineOption {

	public void setValidators(List<CmdLineOptionValidator> validators);

	public List<CmdLineOptionValidator> getValidators();

}

package org.apache.oodt.cas.cl.option;

public class CmdLineSubOption {

	private CmdLineOption option;
	private boolean required;

	public CmdLineSubOption() {}

	public CmdLineSubOption(CmdLineOption option, boolean required) {
		this.option = option;
		this.required = required;
	}

	public void setOption(CmdLineOption option) {
		this.option = option;
	}

	public CmdLineOption getOption() {
		return option;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRequired() {
		return required;
	}
}

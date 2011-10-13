package org.apache.oodt.cas.cl.option;

import java.util.Set;

public class GroupCmdLineOption extends SimpleCmdLineOption {

	private Set<SubOption> subOptions;

	public GroupCmdLineOption() {
		super();
		this.setHasArgs(false);
	}

	public GroupCmdLineOption(String shortOption, String longOption,
			String description, boolean hasArgs) {
		super(shortOption, longOption, description, hasArgs);
	}

	public void setSubOptions(Set<SubOption> subOptions) {
		this.subOptions = subOptions;
	}

	public Set<SubOption> getSubOptions() {
		return subOptions;
	}

	public boolean hasSubOptions() {
		return subOptions != null && !subOptions.isEmpty();
	}

	public class SubOption {

		private CmdLineOption option;
		private boolean required;

		public SubOption() {}

		public SubOption(CmdLineOption option, boolean required) {
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
}

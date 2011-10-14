package org.apache.oodt.cas.cl.option;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

public class GroupCmdLineOption extends SimpleCmdLineOption {

	private Set<SubOption> subOptions;
	private boolean allowAnySubOption;

	public GroupCmdLineOption() {
		super();
		this.setHasArgs(false);
		this.setAllowAnySubOptions(false);
		subOptions = new HashSet<SubOption>();
	}

	public GroupCmdLineOption(String shortOption, String longOption,
			String description, boolean hasArgs) {
		super(shortOption, longOption, description, hasArgs);
	}

	public void setAllowAnySubOptions(boolean allowAnySubOption) {
		this.allowAnySubOption = allowAnySubOption;
	}

	public boolean isAllowAnySubOptions() {
		return subOptions.isEmpty() && allowAnySubOption;
	}

	public void setSubOptions(Set<SubOption> subOptions) {
		Validate.notNull(subOptions, "Cannot set subOptions to NULL");

		this.subOptions = new HashSet<SubOption>(subOptions);
	}

	public void addSubOption(SubOption subOption) {
		Validate.notNull(subOption, "Cannot add NULL subOption");

		subOptions.add(subOption);
	}

	public Set<SubOption> getSubOptions() {
		return subOptions;
	}

	public boolean hasSubOptions() {
		return subOptions != null && !subOptions.isEmpty();
	}

	public static class SubOption {

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

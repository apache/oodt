package org.apache.oodt.cas.cl.option.require;

import java.util.HashSet;
import java.util.Set;

import org.apache.oodt.cas.cl.action.CmdLineAction;

public class ActionBasedRequirementRule implements RequirementRule {

	private Set<String> actionNames;

	public void setActions(Set<String> actionNames) {
		this.actionNames = new HashSet<String>(actionNames);
	}

	public Set<String> getActions() {
		return actionNames;
	}

	public boolean isRequired(CmdLineAction selectedAction) {
		return actionNames.contains(selectedAction.getName());
	}

}
